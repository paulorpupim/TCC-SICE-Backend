package com.tccds.sice.modules.bot_whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tccds.sice.modules.bot_whatsapp.dto.EnviarAvisoBotDTO;
import com.tccds.sice.modules.envio_aviso.EnvioAviso;
import com.tccds.sice.modules.evento.Evento;

@Service
public class BotWhatsappService {

    @Value("${bot.url}")
    private String botUrl;

    @Value("${bot.secret}")
    private String botSecret;

    public void enviar(EnvioAviso envio) {

        Evento evento = envio.getEvento();

        String mensagem =
                evento.getTitulo()
                + "\nData: "
                + evento.getDataInicio();

        EnviarAvisoBotDTO dto =
                new EnviarAvisoBotDTO(
                        envio.getGrupoWhatsapp()
                                .getIdentificador(),
                        mensagem
                );

        RestClient.create()
                .post()
                .uri(botUrl + "/enviar-aviso")
                .header(
                        "X-Bot-Secret",
                        botSecret
                )
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
}