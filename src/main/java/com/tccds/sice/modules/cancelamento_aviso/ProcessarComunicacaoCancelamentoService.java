package com.tccds.sice.modules.cancelamento_aviso;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tccds.sice.modules.bot_whatsapp.BotWhatsappService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Chamadas HTTP fora de transacoes; nenhuma tentativa automatica apos falha incerta. */
@Service @RequiredArgsConstructor @Slf4j
public class ProcessarComunicacaoCancelamentoService {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private final ComunicacaoCancelamentoRepository repository;
    private final ComunicacaoCancelamentoTransacionalService transacional;
    private final BotWhatsappService bot;

    public void processarDoEvento(Long eventoId) {
        for (Long id : repository.buscarIds(eventoId, StatusComunicacaoCancelamento.PENDENTE)) {
            try {
                processarPorId(id);
            } catch (Exception ex) {
                log.error("[SICE-CANCELAMENTO] Falha inesperada ao processar comunicacao={}", id, ex);
            }
        }
    }

    public void processarPorId(Long id) {
        ReservaComunicacaoCancelamento reserva = transacional.reservar(id);
        if (reserva == null) return;
        boolean confirmado = false;
        String erro = null;
        try {
            bot.enviar(reserva.mensagem());
            confirmado = true;
        } catch (Exception ex) {
            // Timeout/erro HTTP pode ocorrer depois de o WhatsApp publicar a mensagem.
            // Por seguranca nao ha retry automatico para esta comunicacao.
            erro = ex.getMessage();
            log.warn("[SICE-CANCELAMENTO] Bot sem confirmacao da comunicacao={}", id);
        }
        transacional.concluir(reserva, confirmado, erro);
    }

    /** Recupera apenas comunicacoes que nunca iniciaram tentativa (p.ex. apos reinicio).
     *  Um resultado incerto nunca volta para PENDENTE. */
    @Scheduled(cron = "0 */5 * * * *", zone = "America/Sao_Paulo")
    public void despacharNaoIniciadas() {
        for (Long id : repository.buscarPendentesNaoIniciadas(StatusComunicacaoCancelamento.PENDENTE)) {
            try {
                processarPorId(id);
            } catch (Exception ex) {
                log.error("[SICE-CANCELAMENTO] Falha ao despachar comunicacao pendente={}", id, ex);
            }
        }
    }

    @Scheduled(cron = "0 */5 * * * *", zone = "America/Sao_Paulo")
    public void verificarReservasExpiradas() {
        LocalDateTime limite = LocalDateTime.now(ZONA).minusMinutes(15);
        for (Long id : repository.buscarReservasExpiradas(limite)) {
            try {
                transacional.marcarExpirada(id, limite);
            } catch (Exception ex) {
                log.error("[SICE-CANCELAMENTO] Falha ao recuperar reserva={}", id, ex);
            }
        }
    }
}
