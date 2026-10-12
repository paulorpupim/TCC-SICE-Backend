package com.tccds.sice.modules.bot_whatsapp;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.bot_whatsapp.dto.EnviarAvisoBotDTO;
import com.tccds.sice.modules.envio_aviso.EnvioAviso;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.evento_destino.EventoDestino;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;

@Service
public class BotWhatsappService {

        @Value("${bot.url}")
        private String botUrl;

        @Value("${bot.secret}")
        private String botSecret;

        public void enviar(EnvioAviso envio) {

                Evento evento = envio.getEvento();
                GrupoWhatsapp grupo = envio.getGrupoWhatsapp();

                String mensagem = montarMensagem(evento, grupo);

                EnviarAvisoBotDTO dto = new EnviarAvisoBotDTO(
                                grupo.getIdentificador(),
                                mensagem);

                RestClient.create()
                                .post()
                                .uri(botUrl + "/enviar-aviso")
                                .header(
                                                "X-Bot-Secret",
                                                botSecret)
                                .body(dto)
                                .retrieve()
                                .toBodilessEntity();
        }

        private String montarMensagem(
                        Evento evento,
                        GrupoWhatsapp grupo) {

                DateTimeFormatter formatoData = DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy");

                DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern(
                                "HH:mm");

                String descricao = evento.getDescricao() != null
                                && !evento.getDescricao().isBlank()
                                                ? evento.getDescricao()
                                                : "Sem descrição informada.";

                String publicoAlvo = montarPublicoAlvo(
                                evento,
                                grupo);

                return """
                                📢 *AVISO DE EVENTO*

                                📌 *Evento:* %s
                                📅 *Data:* %s
                                🕒 *Horário:* %s
                                %s
                                📝 *Descrição:*
                                %s
                                """.formatted(
                                evento.getTitulo(),
                                evento.getDataInicio()
                                                .format(formatoData),
                                evento.getHoraInicio()
                                                .format(formatoHora),
                                publicoAlvo,
                                descricao);
        }

        private String montarPublicoAlvo(
                        Evento evento,
                        GrupoWhatsapp grupo) {

                if (grupo.getTipo() != TipoGrupo.GERAL_ALUNOS) {

                        return "";
                }

                EventoDestino destinoAluno = evento.getDestinos()
                                .stream()
                                .filter(destino -> destino.getPerfil() == PerfilUsuario.ALUNO)
                                .findFirst()
                                .orElse(null);

                if (destinoAluno == null) {
                        return "";
                }

                boolean semEtapas = destinoAluno.getEtapas().isEmpty();

                boolean semModalidades = destinoAluno
                                .getModalidades()
                                .isEmpty();

                if (semEtapas && semModalidades) {

                        return """

                                        🎯 *Destinado a:* Todos os alunos

                                        """;
                }

                StringBuilder publico = new StringBuilder(
                                "\n🎯 *Destinado a:*\n");

                if (!semEtapas) {

                        String etapas = destinoAluno.getEtapas()
                                        .stream()
                                        .sorted()
                                        .map(this::formatarEtapa)
                                        .collect(
                                                        Collectors.joining(", "));

                        publico
                                        .append("• Etapa: ")
                                        .append(etapas)
                                        .append("\n");
                }

                if (!semModalidades) {

                        String modalidades = destinoAluno.getModalidades()
                                        .stream()
                                        .sorted()
                                        .map(this::formatarModalidade)
                                        .collect(
                                                        Collectors.joining(", "));

                        publico
                                        .append("• Modalidade: ")
                                        .append(modalidades)
                                        .append("\n");
                }

                return publico.toString();
        }

        private String formatarEtapa(Etapa etapa) {

                return switch (etapa) {

                        case PRIMEIRA ->
                                "Primeira";

                        case SEGUNDA ->
                                "Segunda";

                        case TERCEIRA ->
                                "Terceira";
                };
        }

        private String formatarModalidade(
                        ModalidadeEnsino modalidade) {

                return switch (modalidade) {

                        case MEDIO ->
                                "Ensino Médio";

                        case TECNICO ->
                                "Técnico";
                };
        }

}