package com.tccds.sice.modules.envio_aviso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;
import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.bot_whatsapp.BotWhatsappService;
import com.tccds.sice.modules.bot_whatsapp.dto.EnviarAvisoBotDTO;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAviso;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAvisoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transacoes curtas. Nenhum metodo desta classe executa chamadas HTTP.
 * O lock pessimista impede que dois agendadores reservem a mesma linha.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessamentoEnvioTransacionalService {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final int LIMITE_TENTATIVAS = 3;

    private final EnvioAvisoRepository repository;
    private final TentativaEnvioAvisoRepository tentativaRepository;
    private final BotWhatsappService bot;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservaEnvioAviso reservar(Long envioId) {
        EnvioAviso envio = repository.buscarParaReserva(envioId).orElse(null);
        if (envio == null) return null;
        LocalDateTime agora = LocalDateTime.now(ZONA);
        LocalDate hoje = agora.toLocalDate();

        if (envio.getStatus() != StatusEnvioAviso.PENDENTE
                || envio.getProcessandoDesde() != null
                || envio.getTentativas() >= LIMITE_TENTATIVAS
                || envio.getEvento().getStatus() != StatusEvento.ATIVO
                || !envio.getGrupoWhatsapp().isAtivo()
                || envio.getEvento().getDataAviso().isAfter(hoje)
                || envio.getEvento().getDataInicio().isBefore(hoje)
                || (envio.getUltimaTentativaEm() != null
                    && envio.getUltimaTentativaEm().isAfter(agora.minusHours(1)))) {
            return null;
        }

        // Monta a mensagem antes de reservar. Se ela nao puder ser construida,
        // nenhum estado e alterado e nenhuma chamada de rede acontece.
        EnviarAvisoBotDTO mensagem = bot.prepararEnvio(envio);
        int numero = envio.getTentativas() + 1;
        String token = UUID.randomUUID().toString();
        envio.setTentativas(numero);
        envio.setUltimaTentativaEm(agora);
        envio.setProcessandoDesde(agora);
        envio.setReservaToken(token);
        // Dirty checking persiste a reserva no commit ANTES da chamada HTTP.
        log.info("[SICE-AVISOS] Reserva criada: envio={} tentativa={}", envioId, numero);
        return new ReservaEnvioAviso(envioId, token, numero, agora, mensagem);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void concluir(ReservaEnvioAviso reserva, boolean confirmado, String erro) {
        EnvioAviso envio = repository.buscarParaReserva(reserva.envioId()).orElse(null);
        if (envio == null || !reserva.token().equals(envio.getReservaToken())) {
            log.warn("[SICE-AVISOS] Conclusao ignorada: reserva nao e mais valida, envio={}", reserva.envioId());
            return;
        }

        LocalDateTime fim = LocalDateTime.now(ZONA);
        if (confirmado) {
            envio.setStatus(StatusEnvioAviso.ENVIADO);
            envio.setEnviadoEm(fim);
            envio.setUltimoErro(null);
        } else {
            envio.setUltimoErro(limitarErro(erro));
            if (reserva.numeroTentativa() >= LIMITE_TENTATIVAS) {
                envio.setStatus(StatusEnvioAviso.ERRO);
            }
        }
        tentativaRepository.save(new TentativaEnvioAviso(
                envio, reserva.numeroTentativa(), reserva.iniciadaEm(), fim,
                confirmado ? ResultadoTentativaEnvioAviso.CONFIRMADO_PELO_BOT
                           : ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO,
                confirmado ? null : limitarErro(erro)));
        envio.setProcessandoDesde(null);
        envio.setReservaToken(null);
        log.info("[SICE-AVISOS] Reserva concluida: envio={} tentativa={} confirmado={}",
                reserva.envioId(), reserva.numeroTentativa(), confirmado);
    }

    /**
     * Um processo morreu durante a chamada HTTP: NAO reenviar automaticamente,
     * pois a mensagem pode ter chegado ao grupo.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void interromperReservaExpirada(Long envioId, LocalDateTime limite) {
        EnvioAviso envio = repository.buscarParaReserva(envioId).orElse(null);
        if (envio == null || envio.getProcessandoDesde() == null
                || envio.getProcessandoDesde().isAfter(limite)) {
            return;
        }
        String erro = "RESULTADO INCERTO: o processo foi interrompido sem confirmar o envio. "
                + "Verifique o WhatsApp antes de qualquer reenvio.";
        tentativaRepository.save(new TentativaEnvioAviso(envio, envio.getTentativas(),
                envio.getProcessandoDesde(), LocalDateTime.now(ZONA),
                ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO, erro));
        envio.setStatus(StatusEnvioAviso.ERRO);
        envio.setUltimoErro(erro);
        envio.setProcessandoDesde(null);
        envio.setReservaToken(null);
        log.warn("[SICE-AVISOS] Reserva expirou com resultado incerto: envio={}", envioId);
    }

    private String limitarErro(String mensagem) {
        String texto = mensagem == null || mensagem.isBlank()
                ? "O bot nao confirmou a requisicao." : mensagem;
        return texto.length() <= 500 ? texto : texto.substring(0, 500);
    }
}
