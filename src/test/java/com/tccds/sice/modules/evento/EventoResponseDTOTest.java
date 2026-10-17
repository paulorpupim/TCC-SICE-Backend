package com.tccds.sice.modules.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.tccds.sice.modules.evento.dto.EventoResponseDTO;
import com.tccds.sice.modules.usuario.Usuario;

class EventoResponseDTOTest {

    @Test
    void retornaNomeEIdDoUsuarioCriador() {
        Usuario criador = new Usuario();
        criador.setId(7L);
        criador.setNome("Maria Oliveira");

        Evento evento = new Evento(
                "Reuniao de pais",
                "Encontro de responsaveis",
                LocalDate.now().plusDays(10),
                LocalTime.of(14, 0),
                criador);
        evento.setId(15L);

        EventoResponseDTO resposta = new EventoResponseDTO(evento);

        assertEquals(15L, resposta.id());
        assertEquals(7L, resposta.criadoPor());
        assertEquals("Maria Oliveira", resposta.criadoPorNome());
    }
}
