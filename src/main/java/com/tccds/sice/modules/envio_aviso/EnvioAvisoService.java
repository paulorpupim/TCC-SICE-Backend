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
import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.bot_whatsapp.BotWhatsappService;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.evento_destino.EventoDestino;
import com.tccds.sice.modules.evento.evento_destino_turma.EventoDestinoTurma;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsappRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnvioAvisoService {

        private final EnvioAvisoRepository envioAvisoRepository;
        private final GrupoWhatsappRepository grupoWhatsappRepository;
        private final BotWhatsappService botWhatsappService;

        @Transactional
        public void criarEnviosParaEvento(Evento evento) {

                Set<GrupoWhatsapp> grupos = resolverGrupos(evento);

                for (GrupoWhatsapp grupo : grupos) {

                        boolean jaExiste = envioAvisoRepository
                                        .existsByEventoIdAndGrupoWhatsappId(
                                                        evento.getId(),
                                                        grupo.getId());

                        if (jaExiste) {
                                continue;
                        }

                        EnvioAviso envio = new EnvioAviso(
                                        evento,
                                        grupo);

                        envioAvisoRepository.save(envio);
                }
        }

        public void processarEnvio(EnvioAviso envio) {

                if (envio.getStatus() != StatusEnvioAviso.PENDENTE) {

                        return;
                }

                envio.setTentativas(
                                envio.getTentativas() + 1);

                envio.setUltimaTentativaEm(
                                LocalDateTime.now(
                                                ZoneId.of(
                                                                "America/Sao_Paulo")));

                try {

                        botWhatsappService.enviar(envio);

                        envio.setStatus(
                                        StatusEnvioAviso.ENVIADO);

                        envio.setEnviadoEm(
                                        LocalDateTime.now(
                                                        ZoneId.of(
                                                                        "America/Sao_Paulo")));

                        envio.setUltimoErro(null);

                } catch (Exception exception) {

                        envio.setUltimoErro(
                                        exception.getMessage());

                        if (envio.getTentativas() >= 3) {

                                envio.setStatus(
                                                StatusEnvioAviso.ERRO);
                        }
                }
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
        public void sincronizarAposEdicao(
                        Evento evento,
                        boolean dataAlterada) {

                Set<GrupoWhatsapp> gruposAtuais = resolverGrupos(evento);

                List<EnvioAviso> enviosExistentes = envioAvisoRepository
                                .findByEventoId(
                                                evento.getId());

                Set<Long> gruposAtuaisIds = gruposAtuais.stream()
                                .map(GrupoWhatsapp::getId)
                                .collect(Collectors.toSet());

                for (EnvioAviso envio : enviosExistentes) {

                        Long grupoId = envio
                                        .getGrupoWhatsapp()
                                        .getId();

                        if (!gruposAtuaisIds.contains(
                                        grupoId)) {

                                envioAvisoRepository
                                                .delete(envio);
                        }
                }

                for (GrupoWhatsapp grupo : gruposAtuais) {

                        EnvioAviso envioExistente = enviosExistentes.stream()
                                        .filter(envio -> envio
                                                        .getGrupoWhatsapp()
                                                        .getId()
                                                        .equals(
                                                                        grupo.getId()))
                                        .findFirst()
                                        .orElse(null);

                        if (envioExistente == null) {

                                EnvioAviso novoEnvio = new EnvioAviso(
                                                evento,
                                                grupo);

                                envioAvisoRepository
                                                .save(novoEnvio);

                                continue;
                        }

                        if (dataAlterada) {

                                envioExistente.setStatus(
                                                StatusEnvioAviso.PENDENTE);

                                envioExistente.setTentativas(0);

                                envioExistente
                                                .setUltimaTentativaEm(null);

                                envioExistente
                                                .setEnviadoEm(null);

                                envioExistente
                                                .setUltimoErro(null);
                        }
                }
        }

        @Transactional
        public void processarSeDataAvisoChegou(Evento evento) {

                LocalDate hoje = LocalDate.now(
                                ZoneId.of("America/Sao_Paulo"));

                if (evento.getDataAviso().isAfter(hoje)) {
                        return;
                }

                criarEnviosParaEvento(evento);

                processarEnviosPendentesDoEvento(
                                evento.getId());
        }

}