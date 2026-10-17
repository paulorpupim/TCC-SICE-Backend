package com.tccds.sice.modules.envio_aviso;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.bot_whatsapp.BotWhatsappService;
import com.tccds.sice.modules.bot_whatsapp.dto.EnviarAvisoBotDTO;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.EventoRepository;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsappRepository;

@ExtendWith(MockitoExtension.class)
class EnvioAvisoServiceTest {
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    @Mock EnvioAvisoRepository envios;
    @Mock EventoRepository eventos;
    @Mock GrupoWhatsappRepository grupos;
    @Mock BotWhatsappService bot;
    @Mock ProcessamentoEnvioTransacionalService processamento;
    @Mock ApplicationEventPublisher publisher;
    private EnvioAvisoService service;

    @BeforeEach
    void configurar() {
        service = new EnvioAvisoService(envios, eventos, new ResolverGruposAvisoService(grupos), bot, processamento, publisher);
    }

    @Test
    void semReservaNaoChamaBotNemConclui() {
        when(processamento.reservar(40L)).thenReturn(null);
        service.processarEnvioPorId(40L);
        verify(bot, never()).enviar(any(EnviarAvisoBotDTO.class));
        verify(processamento, never()).concluir(any(), anyBoolean(), any());
    }

    @Test
    void chamadaConfirmadaERegistradaComoSucesso() {
        EnviarAvisoBotDTO dto = new EnviarAvisoBotDTO("grupo@g.us", "aviso");
        ReservaEnvioAviso reserva = new ReservaEnvioAviso(40L, "token", 1,
                LocalDateTime.now(ZONA), dto);
        when(processamento.reservar(40L)).thenReturn(reserva);
        service.processarEnvioPorId(40L);
        verify(bot).enviar(dto);
        verify(processamento).concluir(reserva, true, null);
    }

    @Test
    void falhaHttpERegistradaSemTentarNovamenteNoMesmoMetodo() {
        EnviarAvisoBotDTO dto = new EnviarAvisoBotDTO("grupo@g.us", "aviso");
        ReservaEnvioAviso reserva = new ReservaEnvioAviso(40L, "token", 1,
                LocalDateTime.now(ZONA), dto);
        when(processamento.reservar(40L)).thenReturn(reserva);
        doThrow(new IllegalStateException("sem conexao")).when(bot).enviar(dto);
        service.processarEnvioPorId(40L);
        verify(bot, times(1)).enviar(dto);
        verify(processamento).concluir(eq(reserva), eq(false), contains("sem conexao"));
    }

    @Test
    void eventoCanceladoNaoPublicaEnvioImediato() {
        Evento evento = eventoElegivel();
        evento.setStatus(StatusEvento.CANCELADO);
        service.processarSeDataAvisoChegou(evento);
        verifyNoInteractions(publisher, envios, grupos);
    }

    @Test
    void eventoFuturoNaoPublicaEnvioImediato() {
        Evento evento = eventoElegivel();
        evento.setDataAviso(LocalDate.now(ZONA).plusDays(1));
        service.processarSeDataAvisoChegou(evento);
        verifyNoInteractions(publisher, envios, grupos);
    }

    @Test
    void eventoElegivelPublicaEventoParaDepoisDoCommit() {
        Evento evento = eventoElegivel();
        when(eventos.buscarParaCancelamento(10L)).thenReturn(Optional.of(evento));
        service.processarSeDataAvisoChegou(evento);
        verify(publisher).publishEvent(new EnviosImediatosEvent(10L));
        verify(bot, never()).enviar(any(EnviarAvisoBotDTO.class));
    }

    @Test
    void agendadorNaoCriaAvisoSeEventoFoiExcluidoEntreListagemEProcessamento() {
        Evento evento = eventoElegivel();
        // buscarParaCancelamento retorna Optional.empty() em vez de lançar exceção.
        service.criarEnviosParaEvento(evento);
        verify(eventos).buscarParaCancelamento(10L);
        verify(envios, never()).save(any(EnvioAviso.class));
        verifyNoInteractions(grupos);
    }

    @Test
    void criacaoDeAvisosConfereBloqueioDoEventoAntesDeResolverGrupos() {
        Evento evento = eventoElegivel();
        when(eventos.buscarParaCancelamento(10L)).thenReturn(Optional.of(evento));
        service.criarEnviosParaEvento(evento);
        var ordem = inOrder(eventos, grupos);
        ordem.verify(eventos).buscarParaCancelamento(10L);
        // Sem destinos, o resolvedor nao consulta grupos. Ainda assim, o bloqueio ocorreu primeiro.
        verify(envios, never()).save(any(EnvioAviso.class));
    }

    @Test
    void sincronizacaoPreservaAvisoJaEnviadoMesmoQueGrupoSejaRemovido() {
        Evento evento = eventoElegivel();
        EnvioAviso envio = envioDoEvento(evento, StatusEnvioAviso.ENVIADO, 1);
        when(envios.findByEventoId(10L)).thenReturn(List.of(envio));
        service.sincronizarAposEdicao(evento, false);
        assertEquals(StatusEnvioAviso.ENVIADO, envio.getStatus());
        verify(envios, never()).delete(any(EnvioAviso.class));
    }

    @Test
    void grupoRemovidoCancelaSomenteEnvioPendenteNaoIniciado() {
        Evento evento = eventoElegivel();
        EnvioAviso envio = envioDoEvento(evento, StatusEnvioAviso.PENDENTE, 0);
        when(envios.findByEventoId(10L)).thenReturn(List.of(envio));
        service.sincronizarAposEdicao(evento, false);
        assertEquals(StatusEnvioAviso.CANCELADO, envio.getStatus());
        verify(envios, never()).delete(any(EnvioAviso.class));
    }

    @Test
    void edicaoAntecipadaNaoCriaRegistroDeEnvio() {
        Evento evento = eventoElegivel();
        evento.setDataAviso(LocalDate.now(ZONA).plusDays(1));
        GrupoWhatsapp grupo = new GrupoWhatsapp("Alunos", "grupo@g.us", TipoGrupo.GERAL_ALUNOS, null);
        grupo.setId(21L);
        evento.adicionarDestino(new com.tccds.sice.modules.evento.evento_destino.EventoDestino(
                com.tccds.sice.enums.PerfilUsuario.ALUNO, java.util.Set.of(), java.util.Set.of()));
        when(grupos.findByTipoAndAtivoTrue(TipoGrupo.GERAL_ALUNOS)).thenReturn(Optional.of(grupo));
        service.sincronizarAposEdicao(evento, false);
        verify(envios, never()).save(any(EnvioAviso.class));
    }

    @Test
    void edicaoNaDataDeAvisoPodeCriarRegistroDeEnvio() {
        Evento evento = eventoElegivel();
        GrupoWhatsapp grupo = new GrupoWhatsapp("Alunos", "grupo@g.us", TipoGrupo.GERAL_ALUNOS, null);
        grupo.setId(21L);
        evento.adicionarDestino(new com.tccds.sice.modules.evento.evento_destino.EventoDestino(
                com.tccds.sice.enums.PerfilUsuario.ALUNO, java.util.Set.of(), java.util.Set.of()));
        when(grupos.findByTipoAndAtivoTrue(TipoGrupo.GERAL_ALUNOS)).thenReturn(Optional.of(grupo));
        service.sincronizarAposEdicao(evento, false);
        verify(envios).save(any(EnvioAviso.class));
    }

    private Evento eventoElegivel() {
        LocalDate hoje = LocalDate.now(ZONA);
        Evento evento = new Evento("Teste", "Descricao", hoje.plusDays(2), LocalTime.of(8, 0), null);
        evento.setId(10L);
        evento.setDataAviso(hoje);
        return evento;
    }

    private EnvioAviso envioDoEvento(Evento evento, StatusEnvioAviso status, int tentativas) {
        GrupoWhatsapp grupo = new GrupoWhatsapp("Grupo", "grupo@g.us", TipoGrupo.GERAL_ALUNOS, null);
        grupo.setId(20L);
        EnvioAviso envio = new EnvioAviso(evento, grupo);
        envio.setId(30L);
        envio.setStatus(status);
        envio.setTentativas(tentativas);
        return envio;
    }
}
