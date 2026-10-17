package com.tccds.sice.modules.envio_aviso;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.modules.bot_whatsapp.BotWhatsappService;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.EventoRepository;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvioAvisoService {

        private final EnvioAvisoRepository envioAvisoRepository;
        private final EventoRepository eventoRepository;
        private final ResolverGruposAvisoService resolverGruposAvisoService;
        private final BotWhatsappService botWhatsappService;
        private final ProcessamentoEnvioTransacionalService processamentoTransacional;
        private final ApplicationEventPublisher eventPublisher;

        @Transactional
        public void criarEnviosParaEvento(Evento evento) {
                // Serializa a criacao de registros com a exclusao/cancelamento de eventos.
                // Um evento listado pelo scheduler pode ter sido excluido antes de chegar aqui.
                evento = eventoRepository.buscarParaCancelamento(evento.getId()).orElse(null);
                if (evento == null) {
                        log.debug("[SICE-AVISOS] Evento excluido antes de criar avisos.");
                        return;
                }

                if (evento.getStatus() != StatusEvento.ATIVO) {
                        log.debug("[SICE-AVISOS] Nao cria envios: evento={} status={}",
                                        evento.getId(), evento.getStatus());
                        return;
                }

                Set<GrupoWhatsapp> grupos = resolverGruposAvisoService.resolver(evento).grupos();
                log.debug("[SICE-AVISOS] Evento={} destinos={} grupos ATIVOS encontrados={}",
                                evento.getId(), evento.getDestinos().size(), grupos.size());
                if (grupos.isEmpty()) {
                        log.debug("[SICE-AVISOS] Nenhum grupo ativo corresponde aos destinatarios do evento={}",
                                        evento.getId());
                }

                for (GrupoWhatsapp grupo : grupos) {

                        boolean jaExiste = envioAvisoRepository
                                        .existsByEventoIdAndGrupoWhatsappId(
                                                        evento.getId(),
                                                        grupo.getId());

                        if (jaExiste) {
                                log.debug("[SICE-AVISOS] Registro ja existe: evento={} grupo={}",
                                                evento.getId(), grupo.getId());
                                continue;
                        }

                        EnvioAviso envio = new EnvioAviso(
                                        evento,
                                        grupo);

                        EnvioAviso salvo = envioAvisoRepository.save(envio);
                        log.info("[SICE-AVISOS] Registro criado: envio={} evento={} grupo={}",
                                        salvo.getId(), evento.getId(), grupo.getId());
                }
        }

        /** Sem @Transactional: a chamada HTTP acontece APOS o commit da reserva. */
        public void processarEnvioPorId(Long id) {
                ReservaEnvioAviso reserva = processamentoTransacional.reservar(id);
                if (reserva == null) {
                        log.debug("[SICE-AVISOS] Nao reservado: envio={}", id);
                        return;
                }

                boolean confirmado = false;
                String detalheErro = null;
                try {
                        botWhatsappService.enviar(reserva.mensagem());
                        confirmado = true;
                } catch (Exception exception) {
                        // Falha HTTP pode significar resultado desconhecido no WhatsApp.
                        detalheErro = exception.getMessage();
                        log.warn("[SICE-AVISOS] Bot sem confirmacao: envio={} tentativa={} causa={}",
                                id, reserva.numeroTentativa(), detalheErro);
                }
                // Nova transacao curta. Falha aqui NAO provoca nova chamada ao bot.
                processamentoTransacional.concluir(reserva, confirmado, detalheErro);
        }

        /** Seleciona somente IDs, sem reter transacao JPA durante as chamadas ao bot. */
        public void processarEnviosPendentesDoEvento(Long eventoId) {
                List<Long> ids = envioAvisoRepository.buscarIdsDoEventoPorStatus(
                                eventoId, StatusEnvioAviso.PENDENTE);
                for (Long envioId : ids) {
                        try {
                                processarEnvioPorId(envioId);
                        } catch (Exception ex) {
                                log.error("[SICE-AVISOS] Erro ao processar envio={}. "
                                        + "Se havia reserva, sera recuperada como resultado incerto.", envioId, ex);
                        }
                }
        }

        /** Processo agendado para tratar reservas abandonadas por queda do servidor. */
        public void recuperarReservasExpiradas() {
                java.time.LocalDateTime limite = java.time.LocalDateTime.now(
                                ZoneId.of("America/Sao_Paulo")).minusMinutes(15);
                for (Long id : envioAvisoRepository.buscarReservasExpiradas(limite)) {
                        try {
                                processamentoTransacional.interromperReservaExpirada(id, limite);
                        } catch (Exception ex) {
                                log.error("[SICE-AVISOS] Falha ao verificar reserva expirada: envio={}", id, ex);
                        }
                }
        }

        @Transactional
        public void sincronizarAposEdicao(Evento evento, boolean dataAlterada) {
                Set<GrupoWhatsapp> gruposAtuais = resolverGruposAvisoService.resolver(evento).grupos();
                List<EnvioAviso> enviosExistentes = envioAvisoRepository.findByEventoId(evento.getId());
                Set<Long> idsAtuais = gruposAtuais.stream()
                                .map(GrupoWhatsapp::getId)
                                .collect(Collectors.toSet());

                for (EnvioAviso envio : enviosExistentes) {
                        boolean removido = !idsAtuais.contains(envio.getGrupoWhatsapp().getId());
                        if (removido) {
                                // Nao apaga o registro ou o historico de tentativas.
                                if (envio.getStatus() == StatusEnvioAviso.PENDENTE
                                                && envio.getProcessandoDesde() == null) {
                                        envio.setStatus(StatusEnvioAviso.CANCELADO);
                                }
                                continue;
                        }
                        if (dataAlterada && envio.getStatus() == StatusEnvioAviso.PENDENTE
                                        && envio.getProcessandoDesde() == null
                                        && envio.getTentativas() > 0) {
                                // Nao usar tentativas do aviso antigo para enviar novos dados.
                                envio.setStatus(StatusEnvioAviso.CANCELADO);
                        }
                }

                // Edicao antes da data de aviso nao cria registros apenas por alterar destinos.
                // Registros ja existentes (historicos ou pendentes) continuam sincronizados.
                LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
                boolean avisoElegivel = evento.getStatus() == StatusEvento.ATIVO
                                && !evento.getDataAviso().isAfter(hoje)
                                && !evento.getDataInicio().isBefore(hoje);
                for (GrupoWhatsapp grupo : gruposAtuais) {
                        EnvioAviso existente = enviosExistentes.stream()
                                        .filter(e -> e.getGrupoWhatsapp().getId().equals(grupo.getId()))
                                        .findFirst().orElse(null);
                        if (existente == null) {
                                // Sem registro anterior, so criar se ja estiver no periodo de aviso.
                                if (avisoElegivel) {
                                        envioAvisoRepository.save(new EnvioAviso(evento, grupo));
                                }
                        } else if (existente.getStatus() == StatusEnvioAviso.CANCELADO
                                        && existente.getTentativas() == 0) {
                                // Reabilita o envio nunca tentado se o grupo foi readicionado.
                                existente.setStatus(StatusEnvioAviso.PENDENTE);
                        }
                }
        }

        @Transactional
        public void processarSeDataAvisoChegou(Evento evento) {

                LocalDate hoje = LocalDate.now(
                                ZoneId.of("America/Sao_Paulo"));

                if (evento.getStatus() != StatusEvento.ATIVO
                                || evento.getDataAviso().isAfter(hoje)
                                || evento.getDataInicio().isBefore(hoje)) {
                        log.debug("[SICE-AVISOS] Imediato ignorado: evento={} status={} dataAviso={} dataInicio={} hoje={}",
                                        evento.getId(), evento.getStatus(), evento.getDataAviso(),
                                        evento.getDataInicio(), hoje);
                        return;
                }
                log.debug("[SICE-AVISOS] Imediato elegivel: evento={}", evento.getId());

                criarEnviosParaEvento(evento);

                // EventoService pode estar no meio de um @Transactional.
                // O listener disparara SOMENTE depois do commit bem-sucedido.
                eventPublisher.publishEvent(new EnviosImediatosEvent(evento.getId()));
        }

}