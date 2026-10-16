package com.tccds.sice.modules.cancelamento_aviso;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.exception.RegraNegocioEventoException;
import com.tccds.sice.modules.envio_aviso.EnvioAviso;
import com.tccds.sice.modules.envio_aviso.EnvioAvisoRepository;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.EventoRepository;
import com.tccds.sice.modules.evento.dto.EventoResponseDTO;
import com.tccds.sice.modules.cancelamento_aviso.dto.CancelamentoComAvisoResponseDTO;

import lombok.RequiredArgsConstructor;

/**
 * Fluxo explicito: PATCH /eventos/{id}/cancelar-com-aviso.
 * O PATCH /cancelar tradicional nao envia WhatsApp e continua disponivel.
 */
@Service @RequiredArgsConstructor
public class ComunicacaoCancelamentoService {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final EventoRepository eventoRepository;
    private final EnvioAvisoRepository envioRepository;
    private final ComunicacaoCancelamentoRepository cancelamentoRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public CancelamentoComAvisoResponseDTO cancelarEComunicar(Long id) {
        // Serializa duas requisicoes de cancelamento para o mesmo evento.
        Evento evento = eventoRepository.buscarParaCancelamento(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Evento nao encontrado."));
        if (evento.getStatus() != StatusEvento.ATIVO) {
            throw new RegraNegocioEventoException("Apenas eventos ativos podem ser cancelados.");
        }
        if (cancelamentoRepository.existsByEventoId(id)) {
            throw new RegraNegocioEventoException("Ja existe comunicacao de cancelamento para este evento.");
        }

        // Bloqueia os envios originais por ID (mesmo lock do processador).
        // Se algum ja estiver em andamento, nao cancela ate que ele termine:
        // senao os destinatarios poderiam receber o aviso original apos o cancelamento.
        List<EnvioAviso> originais = envioRepository.findByEventoIdOrderByIdAsc(id).stream()
                .map(envio -> envioRepository.buscarParaReserva(envio.getId()).orElseThrow())
                .toList();
        if (originais.stream().anyMatch(e -> e.getProcessandoDesde() != null)) {
            throw new RegraNegocioEventoException(
                    "Existe aviso original em processamento. Tente cancelar novamente apos sua conclusao.");
        }
        evento.setStatus(StatusEvento.CANCELADO);

        int criados = 0;
        int gruposInativos = 0;
        LocalDateTime agora = LocalDateTime.now(ZONA);
        if (evento.getDataInicio().atTime(evento.getHoraInicio()).isAfter(agora)) {
            for (EnvioAviso original : originais) {
                if (original.getStatus() != StatusEnvioAviso.ENVIADO) continue;
                if (!original.getGrupoWhatsapp().isAtivo()) {
                    gruposInativos++;
                    continue;
                }
                String texto = mensagem(evento);
                ComunicacaoCancelamento c = new ComunicacaoCancelamento(evento,
                        original.getGrupoWhatsapp(), original.getId(), texto, agora);
                cancelamentoRepository.save(c);
                criados++;
            }
        }
        // AFTER_COMMIT: erro HTTP nao pode desfazer o cancelamento confirmado.
        if (criados > 0) publisher.publishEvent(new CancelamentoComunicacaoEvent(evento.getId()));
        return new CancelamentoComAvisoResponseDTO(new EventoResponseDTO(evento), criados, gruposInativos);
    }

    private String mensagem(Evento evento) {
        return """
                🚨 *AVISO DE CANCELAMENTO*

                O evento *%s*, previsto para *%s às %s*, foi cancelado.
                O evento não será realizado conforme a programação anterior.

                Comunicado automático — SICE
                """.formatted(evento.getTitulo(), evento.getDataInicio().format(DATA),
                              evento.getHoraInicio().format(HORA));
    }
}
