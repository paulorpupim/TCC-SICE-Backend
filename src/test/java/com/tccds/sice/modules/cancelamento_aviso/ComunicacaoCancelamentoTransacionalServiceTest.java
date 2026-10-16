package com.tccds.sice.modules.cancelamento_aviso;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;

@ExtendWith(MockitoExtension.class)
class ComunicacaoCancelamentoTransacionalServiceTest {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    @Mock ComunicacaoCancelamentoRepository repository;
    @Mock TentativaComunicacaoCancelamentoRepository tentativas;

    private ComunicacaoCancelamentoTransacionalService service;
    private ComunicacaoCancelamento comunicacao;

    @BeforeEach void preparar() {
        service = new ComunicacaoCancelamentoTransacionalService(repository, tentativas);
        Evento evento = new Evento("Reuniao", "Descricao", LocalDate.now(ZONA).plusDays(2),
                LocalTime.of(17, 0), null);
        evento.setId(9L);
        evento.setStatus(StatusEvento.CANCELADO);
        GrupoWhatsapp grupo = new GrupoWhatsapp("Teste", "grupo-test@g.us", TipoGrupo.GERAL_ALUNOS, null);
        grupo.setId(20L);
        comunicacao = new ComunicacaoCancelamento(evento, grupo, 77L, "CANCELAMENTO", LocalDateTime.now(ZONA));
        comunicacao.setId(30L);
    }

    private void encontrar() {
        when(repository.buscarParaReserva(30L)).thenReturn(Optional.of(comunicacao));
    }

    @Test void reservaSoUmaVez() {
        encontrar();
        var reserva = service.reservar(30L);
        assertNotNull(reserva);
        assertEquals("CANCELAMENTO", reserva.mensagem().mensagem());
        assertEquals(1, comunicacao.getTentativas());
        assertNotNull(comunicacao.getReservaToken());
        assertNull(service.reservar(30L));
    }

    @Test void bloqueiaEventoReativado() {
        comunicacao.getEvento().setStatus(StatusEvento.ATIVO);
        encontrar();
        assertNull(service.reservar(30L));
        assertEquals(StatusComunicacaoCancelamento.CANCELADO, comunicacao.getStatus());
        verifyNoInteractions(tentativas);
    }

    @Test void bloqueiaGrupoDesativado() {
        comunicacao.getGrupoWhatsapp().setAtivo(false);
        encontrar();
        assertNull(service.reservar(30L));
        assertEquals(StatusComunicacaoCancelamento.CANCELADO, comunicacao.getStatus());
    }

    @Test void sucessoPreservaHistorico() {
        encontrar();
        var reserva = service.reservar(30L);
        service.concluir(reserva, true, null);
        assertEquals(StatusComunicacaoCancelamento.ENVIADO, comunicacao.getStatus());
        assertNull(comunicacao.getReservaToken());
        assertNotNull(comunicacao.getEnviadoEm());
        ArgumentCaptor<TentativaComunicacaoCancelamento> cap =
            ArgumentCaptor.forClass(TentativaComunicacaoCancelamento.class);
        verify(tentativas).save(cap.capture());
        assertEquals(ResultadoTentativaEnvioAviso.CONFIRMADO_PELO_BOT, cap.getValue().getResultado());
    }

    @Test void falhaNaoReenviaAutomaticamente() {
        encontrar();
        var reserva = service.reservar(30L);
        service.concluir(reserva, false, "timeout");
        assertEquals(StatusComunicacaoCancelamento.ERRO, comunicacao.getStatus());
        assertNull(service.reservar(30L));
        verify(tentativas).save(any(TentativaComunicacaoCancelamento.class));
    }

    @Test void expiracaoNaoDisparaOutroEnvio() {
        encontrar();
        service.reservar(30L);
        comunicacao.setProcessandoDesde(LocalDateTime.now(ZONA).minusMinutes(30));
        service.marcarExpirada(30L, LocalDateTime.now(ZONA).minusMinutes(15));
        assertEquals(StatusComunicacaoCancelamento.ERRO, comunicacao.getStatus());
        assertNull(comunicacao.getReservaToken());
        verify(tentativas).save(any(TentativaComunicacaoCancelamento.class));
    }
}
