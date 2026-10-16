package com.tccds.sice.modules.envio_aviso;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.bot_whatsapp.BotWhatsappService;
import com.tccds.sice.modules.bot_whatsapp.dto.EnviarAvisoBotDTO;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAviso;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAvisoRepository;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;

@ExtendWith(MockitoExtension.class)
class ProcessamentoEnvioTransacionalServiceTest {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Mock EnvioAvisoRepository repository;
    @Mock TentativaEnvioAvisoRepository tentativaRepository;
    @Mock BotWhatsappService bot;

    private ProcessamentoEnvioTransacionalService service;
    private EnvioAviso envio;

    @BeforeEach
    void configurar() {
        service = new ProcessamentoEnvioTransacionalService(repository, tentativaRepository, bot);
        LocalDate hoje = LocalDate.now(ZONA);
        Evento evento = new Evento("Reuniao", "Descricao", hoje.plusDays(2), LocalTime.of(14, 0), null);
        evento.setId(100L);
        evento.setDataAviso(hoje);
        GrupoWhatsapp grupo = new GrupoWhatsapp("Grupo de teste", "grupo-test@g.us", TipoGrupo.GERAL_ALUNOS, null);
        grupo.setId(200L);
        envio = new EnvioAviso(evento, grupo);
        envio.setId(300L);
    }

    private void encontrarEnvio() {
        when(repository.buscarParaReserva(300L)).thenReturn(Optional.of(envio));
    }

    private ReservaEnvioAviso reservaSimulada() {
        envio.setTentativas(1);
        envio.setProcessandoDesde(LocalDateTime.now(ZONA).minusMinutes(1));
        envio.setReservaToken("token-123");
        return new ReservaEnvioAviso(300L, "token-123", 1,
                envio.getProcessandoDesde(), new EnviarAvisoBotDTO("grupo-test@g.us", "Mensagem"));
    }

    @Test
    void naoReservaEnvioInexistente() {
        when(repository.buscarParaReserva(300L)).thenReturn(Optional.empty());
        assertNull(service.reservar(300L));
        verifyNoInteractions(bot, tentativaRepository);
    }

    @Test
    void bloqueiaEnvioJaConcluido() {
        envio.setStatus(StatusEnvioAviso.ENVIADO);
        encontrarEnvio();
        assertNull(service.reservar(300L));
        verifyNoInteractions(bot);
    }

    @Test
    void bloqueiaEnvioJaReservado() {
        envio.setProcessandoDesde(LocalDateTime.now(ZONA));
        encontrarEnvio();
        assertNull(service.reservar(300L));
        verifyNoInteractions(bot);
    }

    @Test
    void bloqueiaLimiteDeTresTentativas() {
        envio.setTentativas(3);
        encontrarEnvio();
        assertNull(service.reservar(300L));
    }

    @Test
    void bloqueiaEventoCancelado() {
        envio.getEvento().setStatus(StatusEvento.CANCELADO);
        encontrarEnvio();
        assertNull(service.reservar(300L));
    }

    @Test
    void bloqueiaGrupoDesativado() {
        envio.getGrupoWhatsapp().setAtivo(false);
        encontrarEnvio();
        assertNull(service.reservar(300L));
    }

    @Test
    void bloqueiaAvisoProgramadoParaDepoisDeHoje() {
        envio.getEvento().setDataAviso(LocalDate.now(ZONA).plusDays(1));
        encontrarEnvio();
        assertNull(service.reservar(300L));
    }

    @Test
    void bloqueiaEventoQueJaPassou() {
        envio.getEvento().setDataInicio(LocalDate.now(ZONA).minusDays(1));
        encontrarEnvio();
        assertNull(service.reservar(300L));
    }

    @Test
    void respeitaIntervaloMinimoDeUmaHora() {
        envio.setUltimaTentativaEm(LocalDateTime.now(ZONA).minusMinutes(30));
        encontrarEnvio();
        assertNull(service.reservar(300L));
    }

    @Test
    void reservaAposIntervaloSemChamarHttp() {
        envio.setUltimaTentativaEm(LocalDateTime.now(ZONA).minusHours(2));
        encontrarEnvio();
        EnviarAvisoBotDTO dto = new EnviarAvisoBotDTO("grupo-test@g.us", "Mensagem");
        when(bot.prepararEnvio(envio)).thenReturn(dto);

        ReservaEnvioAviso reserva = service.reservar(300L);

        assertNotNull(reserva);
        assertEquals(1, reserva.numeroTentativa());
        assertEquals(1, envio.getTentativas());
        assertEquals(reserva.token(), envio.getReservaToken());
        assertNotNull(envio.getProcessandoDesde());
        assertSame(dto, reserva.mensagem());
        verify(bot, never()).enviar(any(EnviarAvisoBotDTO.class));
        verifyNoInteractions(tentativaRepository);
    }

    @Test
    void confirmaEnvioELimpaReservaRegistrandoTentativa() {
        ReservaEnvioAviso reserva = reservaSimulada();
        encontrarEnvio();
        service.concluir(reserva, true, null);

        assertEquals(StatusEnvioAviso.ENVIADO, envio.getStatus());
        assertNotNull(envio.getEnviadoEm());
        assertNull(envio.getUltimoErro());
        assertNull(envio.getProcessandoDesde());
        assertNull(envio.getReservaToken());
        ArgumentCaptor<TentativaEnvioAviso> captor = ArgumentCaptor.forClass(TentativaEnvioAviso.class);
        verify(tentativaRepository).save(captor.capture());
        assertEquals(ResultadoTentativaEnvioAviso.CONFIRMADO_PELO_BOT, captor.getValue().getResultado());
        assertEquals(1, captor.getValue().getNumeroTentativa());
    }

    @Test
    void falhaInicialPermanecePendenteEMantemHistorico() {
        ReservaEnvioAviso reserva = reservaSimulada();
        encontrarEnvio();
        service.concluir(reserva, false, "Bot indisponivel");

        assertEquals(StatusEnvioAviso.PENDENTE, envio.getStatus());
        assertEquals("Bot indisponivel", envio.getUltimoErro());
        assertNull(envio.getReservaToken());
        ArgumentCaptor<TentativaEnvioAviso> captor = ArgumentCaptor.forClass(TentativaEnvioAviso.class);
        verify(tentativaRepository).save(captor.capture());
        assertEquals(ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO, captor.getValue().getResultado());
    }

    @Test
    void falhaNaTerceiraTentativaEncerraComoErro() {
        ReservaEnvioAviso reserva = reservaSimulada();
        envio.setTentativas(3);
        encontrarEnvio();
        ReservaEnvioAviso terceira = new ReservaEnvioAviso(300L, reserva.token(), 3,
                reserva.iniciadaEm(), reserva.mensagem());
        service.concluir(terceira, false, "Sem resposta");
        assertEquals(StatusEnvioAviso.ERRO, envio.getStatus());
        verify(tentativaRepository).save(any(TentativaEnvioAviso.class));
    }

    @Test
    void ignoraConclusaoComTokenAntigo() {
        ReservaEnvioAviso reserva = reservaSimulada();
        encontrarEnvio();
        envio.setReservaToken("token-atual-diferente");
        service.concluir(reserva, true, null);
        assertEquals(StatusEnvioAviso.PENDENTE, envio.getStatus());
        verifyNoInteractions(tentativaRepository);
    }

    @Test
    void reservaExpiradaGeraResultadoIncertoSemReenvio() {
        ReservaEnvioAviso reserva = reservaSimulada();
        envio.setProcessandoDesde(LocalDateTime.now(ZONA).minusMinutes(30));
        encontrarEnvio();
        service.interromperReservaExpirada(300L, LocalDateTime.now(ZONA).minusMinutes(15));
        assertEquals(StatusEnvioAviso.ERRO, envio.getStatus());
        assertTrue(envio.getUltimoErro().contains("RESULTADO INCERTO"));
        assertNull(envio.getReservaToken());
        ArgumentCaptor<TentativaEnvioAviso> captor = ArgumentCaptor.forClass(TentativaEnvioAviso.class);
        verify(tentativaRepository).save(captor.capture());
        assertEquals(ResultadoTentativaEnvioAviso.SEM_CONFIRMACAO, captor.getValue().getResultado());
        verify(bot, never()).enviar(any(EnviarAvisoBotDTO.class));
    }

    @Test
    void reservaNaoExpiradaNaoSofreMudancas() {
        reservaSimulada();
        encontrarEnvio();
        service.interromperReservaExpirada(300L, LocalDateTime.now(ZONA).minusMinutes(15));
        assertEquals(StatusEnvioAviso.PENDENTE, envio.getStatus());
        verifyNoInteractions(tentativaRepository);
    }
}
