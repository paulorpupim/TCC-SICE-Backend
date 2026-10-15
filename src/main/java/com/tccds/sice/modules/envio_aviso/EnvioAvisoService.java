package com.tccds.sice.modules.envio_aviso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.bot_whatsapp.BotWhatsappService;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAviso;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAvisoRepository;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.evento_destino.EventoDestino;
import com.tccds.sice.modules.evento.evento_destino_turma.EventoDestinoTurma;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsappRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvioAvisoService {

        private final EnvioAvisoRepository envioAvisoRepository;
        private final TentativaEnvioAvisoRepository tentativaEnvioAvisoRepository;
        private final GrupoWhatsappRepository grupoWhatsappRepository;
        private final BotWhatsappService botWhatsappService;

        @Transactional
        public void criarEnviosParaEvento(Evento evento) {

                if (evento.getStatus() != StatusEvento.ATIVO) {
                        log.debug("[SICE-AVISOS] Nao cria envios: evento={} status={}",
                                        evento.getId(), evento.getStatus());
                        return;
                }

                Set<GrupoWhatsapp> grupos = resolverGrupos(evento);
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

        /**
         * O resultado e a tentativa sao persistidos na MESMA transacao.
         * Esta etapa NAO implementa bloqueio concorrente/idempotencia no bot.
         */
        @Transactional
        public void processarEnvio(EnvioAviso envio) {

                if (!podeProcessar(envio)) {
                        log.debug("[SICE-AVISOS] Nao processado: envio={} status={} tentativas={} eventoStatus={} grupoAtivo={} dataAviso={} dataInicio={}",
                                        envio.getId(), envio.getStatus(), envio.getTentativas(),
                                        envio.getEvento().getStatus(), envio.getGrupoWhatsapp().isAtivo(),
                                        envio.getEvento().getDataAviso(), envio.getEvento().getDataInicio());
                        return;
                }

                LocalDateTime inicio = agora();
                int numeroTentativa = envio.getTentativas() + 1;
                envio.setTentativas(numeroTentativa);
                envio.setUltimaTentativaEm(inicio);

                ResultadoTentativaEnvioAviso resultado;
                String detalheErro = null;
                try {
                        botWhatsappService.enviar(envio);
                        resultado = ResultadoTentativaEnvioAviso.CONFIRMADO_PELO_BOT;
                        envio.setStatus(StatusEnvioAviso.ENVIADO);
                        envio.setEnviadoEm(agora());
                        envio.setUltimoErro(null);
                        log.info("[SICE-AVISOS] Bot confirmou: envio={} tentativa={}",
                                        envio.getId(), numeroTentativa);
                } catch (Exception exception) {
                        // Excecao HTTP nao comprova que o WhatsApp nao recebeu a mensagem.
                        resultado = ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO;
                        detalheErro = limitarErro(exception.getMessage());
                        envio.setUltimoErro(detalheErro);
                        log.warn("[SICE-AVISOS] Sem confirmacao do bot: envio={} tentativa={} motivo={}",
                                        envio.getId(), numeroTentativa, detalheErro);
                        if (numeroTentativa >= 3) {
                                envio.setStatus(StatusEnvioAviso.ERRO);
                        }
                }

                tentativaEnvioAvisoRepository.save(new TentativaEnvioAviso(
                                envio, numeroTentativa, inicio, agora(), resultado, detalheErro));
                envioAvisoRepository.save(envio);
        }

        private boolean podeProcessar(EnvioAviso envio) {
                if (envio.getStatus() != StatusEnvioAviso.PENDENTE
                                || envio.getTentativas() >= 3
                                || envio.getEvento().getStatus() != StatusEvento.ATIVO
                                || !envio.getGrupoWhatsapp().isAtivo()) {
                        return false;
                }

                // O agendador de testes pode rodar a cada 10 segundos; nunca
                // consumir as tres tentativas em sequencia sem intervalo.
                if (envio.getTentativas() > 0
                                && envio.getUltimaTentativaEm() != null
                                && envio.getUltimaTentativaEm().isAfter(agora().minusHours(1))) {
                        log.debug("[SICE-AVISOS] Aguardando intervalo de 1 hora: envio={} ultimaTentativa={}",
                                        envio.getId(), envio.getUltimaTentativaEm());
                        return false;
                }

                LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
                return !envio.getEvento().getDataAviso().isAfter(hoje)
                                && !envio.getEvento().getDataInicio().isBefore(hoje);
        }

        private LocalDateTime agora() {
                return LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        }

        private String limitarErro(String mensagem) {
                String texto = mensagem == null || mensagem.isBlank()
                                ? "Falha sem mensagem retornada pelo serviço de envio."
                                : mensagem;
                return texto.length() <= 500 ? texto : texto.substring(0, 500);
        }

        @Transactional
        public void processarEnvioPorId(Long id) {

                EnvioAviso envio = envioAvisoRepository
                                .findById(id)
                                .orElseThrow();

                processarEnvio(envio);
        }

        @Transactional
        public void processarEnviosPendentesDoEvento(
                        Long eventoId) {

                List<EnvioAviso> envios = envioAvisoRepository
                                .findByEventoIdAndStatus(
                                                eventoId,
                                                StatusEnvioAviso.PENDENTE);

                for (EnvioAviso envio : envios) {

                        processarEnvio(envio);
                }
        }

        private Set<GrupoWhatsapp> resolverGrupos(
                        Evento evento) {

                Set<GrupoWhatsapp> grupos = new HashSet<>();

                for (EventoDestino destino : evento.getDestinos()) {

                        if (destino.getPerfil() == PerfilUsuario.PROFESSOR) {

                                grupoWhatsappRepository
                                                .findByTipoAndAtivoTrue(
                                                                TipoGrupo.GERAL_PROFESSORES)
                                                .ifPresent(grupos::add);

                                continue;
                        }

                        if (destino.getPerfil() == PerfilUsuario.ALUNO) {

                                resolverGruposAlunos(
                                                destino,
                                                grupos);
                        }
                }

                return grupos;
        }

        private void resolverGruposAlunos(
                        EventoDestino destino,
                        Set<GrupoWhatsapp> grupos) {

                if (destino.getDestinacoesTurma()
                                .isEmpty()) {

                        grupoWhatsappRepository
                                        .findByTipoAndAtivoTrue(
                                                        TipoGrupo.GERAL_ALUNOS)
                                        .ifPresent(grupos::add);

                        return;
                }

                for (EventoDestinoTurma destinacao : destino.getDestinacoesTurma()) {

                        Long turmaId = destinacao
                                        .getTurma()
                                        .getId();

                        grupoWhatsappRepository
                                        .findByTurmaIdAndAtivoTrue(
                                                        turmaId)
                                        .ifPresent(grupos::add);
                }
        }

        @Transactional
        public void sincronizarAposEdicao(Evento evento, boolean dataAlterada) {
                Set<GrupoWhatsapp> gruposAtuais = resolverGrupos(evento);
                List<EnvioAviso> enviosExistentes = envioAvisoRepository.findByEventoId(evento.getId());
                Set<Long> idsAtuais = gruposAtuais.stream()
                                .map(GrupoWhatsapp::getId)
                                .collect(Collectors.toSet());

                for (EnvioAviso envio : enviosExistentes) {
                        boolean removido = !idsAtuais.contains(envio.getGrupoWhatsapp().getId());
                        if (removido) {
                                // Nao apaga o registro ou o historico de tentativas.
                                if (envio.getStatus() == StatusEnvioAviso.PENDENTE) {
                                        envio.setStatus(StatusEnvioAviso.CANCELADO);
                                }
                                continue;
                        }
                        if (dataAlterada && envio.getStatus() == StatusEnvioAviso.PENDENTE
                                        && envio.getTentativas() > 0) {
                                // Nao usar tentativas do aviso antigo para enviar novos dados.
                                envio.setStatus(StatusEnvioAviso.CANCELADO);
                        }
                }

                for (GrupoWhatsapp grupo : gruposAtuais) {
                        EnvioAviso existente = enviosExistentes.stream()
                                        .filter(e -> e.getGrupoWhatsapp().getId().equals(grupo.getId()))
                                        .findFirst().orElse(null);
                        if (existente == null) {
                                envioAvisoRepository.save(new EnvioAviso(evento, grupo));
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

                processarEnviosPendentesDoEvento(
                                evento.getId());
        }

}