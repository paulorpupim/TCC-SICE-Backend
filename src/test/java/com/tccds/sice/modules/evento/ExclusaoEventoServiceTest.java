package com.tccds.sice.modules.evento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.exception.RegraNegocioEventoException;
import com.tccds.sice.modules.aluno.AlunoRepository;
import com.tccds.sice.modules.cancelamento_aviso.ComunicacaoCancelamentoRepository;
import com.tccds.sice.modules.envio_aviso.EnvioAviso;
import com.tccds.sice.modules.envio_aviso.EnvioAvisoRepository;
import com.tccds.sice.modules.envio_aviso.EnvioAvisoService;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.modules.turma.TurmaRepository;
import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class ExclusaoEventoServiceTest {
    @Mock EventoRepository eventos;
    @Mock TurmaRepository turmas;
    @Mock AlunoRepository alunos;
    @Mock UsuarioService usuarios;
    @Mock EnvioAvisoService enviosService;
    @Mock EnvioAvisoRepository envios;
    @Mock ComunicacaoCancelamentoRepository cancelamentos;

    private EventoService service;
    private Evento evento;

    @BeforeEach
    void configurar() {
        service = new EventoService(eventos, turmas, alunos, usuarios, enviosService, envios, cancelamentos);
        Usuario criador = mock(Usuario.class);
        lenient().when(criador.getId()).thenReturn(9L);
        evento = new Evento("Evento", "Descricao", LocalDate.now().plusDays(4), LocalTime.of(10, 0), criador);
        evento.setId(10L);
    }

    @Test
    void excluiFisicamenteQuandoNaoHaAvisoNemComunicacao() {
        when(eventos.buscarParaCancelamento(10L)).thenReturn(Optional.of(evento));
        service.excluir(10L);
        verify(envios).existsByEventoId(10L);
        verify(cancelamentos).existsByEventoId(10L);
        verify(eventos).delete(evento);
        verify(eventos).flush();
    }

    @Test
    void qualquerRegistroDeEnvioBloqueiaExclusaoMesmoSemTentativa() {
        when(eventos.buscarParaCancelamento(10L)).thenReturn(Optional.of(evento));
        when(envios.existsByEventoId(10L)).thenReturn(true);
        var erro = assertThrows(RegraNegocioEventoException.class, () -> service.excluir(10L));
        assertTrue(erro.getMessage().contains("cancele"));
        verify(eventos, never()).delete(any());
        verify(cancelamentos, never()).existsByEventoId(anyLong());
    }

    @Test
    void comunicacaoDeCancelamentoImpedeExclusaoMesmoSemEnvioOriginal() {
        when(eventos.buscarParaCancelamento(10L)).thenReturn(Optional.of(evento));
        when(cancelamentos.existsByEventoId(10L)).thenReturn(true);
        assertThrows(RegraNegocioEventoException.class, () -> service.excluir(10L));
        verify(eventos, never()).delete(any());
    }

    @Test
    void eventoInexistenteNaoEExcluido() {
        assertThrows(EntidadeNaoEncontradaException.class, () -> service.excluir(10L));
        verify(eventos, never()).delete(any());
    }

    @Test
    void cancelarSemMensagemRecusaEnvioOriginalEmProcessamento() {
        when(eventos.buscarParaCancelamento(10L)).thenReturn(Optional.of(evento));
        EnvioAviso envio = new EnvioAviso(evento, mock(GrupoWhatsapp.class));
        envio.setId(30L);
        envio.setProcessandoDesde(java.time.LocalDateTime.now());
        when(envios.findByEventoIdOrderByIdAsc(10L)).thenReturn(List.of(envio));
        when(envios.buscarParaReserva(30L)).thenReturn(Optional.of(envio));
        assertThrows(RegraNegocioEventoException.class, () -> service.cancelar(10L));
        assertEquals(StatusEvento.ATIVO, evento.getStatus());
    }

    @Test
    void cancelarSemMensagemFuncionaQuandoNenhumEnvioEstaProcessando() {
        when(eventos.buscarParaCancelamento(10L)).thenReturn(Optional.of(evento));
        var resposta = service.cancelar(10L);
        assertEquals(StatusEvento.CANCELADO, evento.getStatus());
        assertEquals(StatusEvento.CANCELADO, resposta.status());
    }
}
