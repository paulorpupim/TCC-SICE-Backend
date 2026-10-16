package com.tccds.sice.modules.evento;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.tccds.sice.enums.StatusEvento;

class EventoModeloTest {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Test
    void calculaAvisoTresDiasAntesQuandoHaAntecedencia() {
        LocalDate hoje = LocalDate.now(ZONA);
        Evento evento = new Evento("Reuniao", "Teste", hoje.plusDays(10), LocalTime.NOON, null);
        assertEquals(hoje.plusDays(7), evento.getDataAviso());
        assertEquals(StatusEvento.ATIVO, evento.getStatus());
    }

    @Test
    void usaDataAtualQuandoCadastroEProximoDoEvento() {
        LocalDate hoje = LocalDate.now(ZONA);
        Evento evento = new Evento("Reuniao", "Teste", hoje.plusDays(2), LocalTime.NOON, null);
        assertEquals(hoje, evento.getDataAviso());
    }

    @Test
    void recalculaDataQuandoInicioMuda() {
        LocalDate hoje = LocalDate.now(ZONA);
        Evento evento = new Evento("Reuniao", "Teste", hoje.plusDays(10), LocalTime.NOON, null);
        evento.setDataInicio(hoje.plusDays(5));
        evento.calcularDataAviso();
        assertEquals(hoje.plusDays(2), evento.getDataAviso());
    }
}
