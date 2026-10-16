package com.tccds.sice.modules.envio_aviso;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.EventoRepository;

@ExtendWith(MockitoExtension.class)
class AgendamentoAvisoServiceTest {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    @Mock EventoRepository eventos;
    @Mock EnvioAvisoRepository envios;
    @Mock EnvioAvisoService envioService;
    private AgendamentoAvisoService service;

    @BeforeEach
    void configurar() {
        service = new AgendamentoAvisoService(eventos, envios, envioService);
    }

    @Test
    void rotinaDiariaCriaRegistrosEProcessaOsEventosSelecionados() {
        LocalDate hoje = LocalDate.now(ZONA);
        Evento evento = new Evento();
        evento.setId(12L);
        when(eventos.findByStatusAndDataAvisoLessThanEqualAndDataInicioGreaterThanEqual(
                StatusEvento.ATIVO, hoje, hoje)).thenReturn(List.of(evento));
        service.processarAvisosDoDia();
        var ordem = inOrder(envioService);
        ordem.verify(envioService).criarEnviosParaEvento(evento);
        ordem.verify(envioService).processarEnviosPendentesDoEvento(12L);
    }

    @Test
    void retryPassaSomenteIdsElegiveisAoServicoDeReserva() {
        when(envios.buscarIdsPendentesParaRetry(eq(StatusEnvioAviso.PENDENTE),
                eq(StatusEvento.ATIVO), eq(LocalDate.now(ZONA)),
                org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class)))
                .thenReturn(List.of(10L, 11L));
        service.tentarNovamenteEnviosPendentes();
        verify(envioService).processarEnvioPorId(10L);
        verify(envioService).processarEnvioPorId(11L);
        verifyNoMoreInteractions(envioService);
    }

    @Test
    void rotinaDeRecuperacaoDelegaTratamentoDeReservas() {
        service.recuperarReservas();
        verify(envioService).recuperarReservasExpiradas();
    }
}
