package com.tccds.sice.modules.cancelamento_aviso;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.bot_whatsapp.dto.EnviarAvisoBotDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Reservas curtas; nenhuma chamada HTTP ocorre nesta classe. */
@Service @RequiredArgsConstructor @Slf4j
public class ComunicacaoCancelamentoTransacionalService {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private final ComunicacaoCancelamentoRepository repository;
    private final TentativaComunicacaoCancelamentoRepository tentativaRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservaComunicacaoCancelamento reservar(Long id) {
        ComunicacaoCancelamento c = repository.buscarParaReserva(id).orElse(null);
        if (c == null || c.getStatus() != StatusComunicacaoCancelamento.PENDENTE
                || c.getProcessandoDesde() != null || c.getTentativas() != 0) {
            return null;
        }
        // Uma mensagem de cancelamento e permitida somente enquanto o evento
        // continua cancelado e o grupo permanece ativo.
        if (c.getEvento().getStatus() != StatusEvento.CANCELADO || !c.getGrupoWhatsapp().isAtivo()) {
            c.setStatus(StatusComunicacaoCancelamento.CANCELADO);
            c.setUltimoErro("Cancelamento nao enviado: evento ou grupo deixou de ser elegivel.");
            return null;
        }
        LocalDateTime agora = LocalDateTime.now(ZONA);
        if (!c.getEvento().getDataInicio().atTime(c.getEvento().getHoraInicio()).isAfter(agora)) {
            c.setStatus(StatusComunicacaoCancelamento.CANCELADO);
            c.setUltimoErro("Cancelamento nao enviado: horario do evento ja passou.");
            return null;
        }
        c.setTentativas(1);
        c.setProcessandoDesde(agora);
        String token = UUID.randomUUID().toString();
        c.setReservaToken(token);
        return new ReservaComunicacaoCancelamento(id, token, agora,
                new EnviarAvisoBotDTO(c.getIdentificadorGrupo(), c.getMensagem()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void concluir(ReservaComunicacaoCancelamento reserva, boolean confirmado, String erro) {
        ComunicacaoCancelamento c = repository.buscarParaReserva(reserva.comunicacaoId()).orElse(null);
        if (c == null || !reserva.token().equals(c.getReservaToken())) {
            log.warn("[SICE-CANCELAMENTO] Conclusao ignorada para reserva invalida: id={}", reserva.comunicacaoId());
            return;
        }
        LocalDateTime fim = LocalDateTime.now(ZONA);
        c.setStatus(confirmado ? StatusComunicacaoCancelamento.ENVIADO : StatusComunicacaoCancelamento.ERRO);
        c.setUltimoErro(confirmado ? null : limitar(erro));
        if (confirmado) c.setEnviadoEm(fim);
        tentativaRepository.save(new TentativaComunicacaoCancelamento(c, 1, reserva.iniciadaEm(), fim,
                confirmado ? ResultadoTentativaEnvioAviso.CONFIRMADO_PELO_BOT
                          : ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO,
                confirmado ? null : limitar(erro)));
        c.setProcessandoDesde(null);
        c.setReservaToken(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarExpirada(Long id, LocalDateTime limite) {
        ComunicacaoCancelamento c = repository.buscarParaReserva(id).orElse(null);
        if (c == null || c.getProcessandoDesde() == null || c.getProcessandoDesde().isAfter(limite)) return;
        String erro = "RESULTADO INCERTO: nao ha confirmacao da mensagem de cancelamento. Nao reenviar automaticamente.";
        c.setStatus(StatusComunicacaoCancelamento.ERRO);
        c.setUltimoErro(erro);
        tentativaRepository.save(new TentativaComunicacaoCancelamento(c, 1, c.getProcessandoDesde(),
                LocalDateTime.now(ZONA), ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO, erro));
        c.setProcessandoDesde(null);
        c.setReservaToken(null);
    }

    private String limitar(String erro) {
        String msg = erro == null || erro.isBlank() ? "RESULTADO INCERTO: o bot nao confirmou o processamento." :
                "RESULTADO INCERTO: " + erro;
        return msg.substring(0, Math.min(msg.length(), 500));
    }
}
