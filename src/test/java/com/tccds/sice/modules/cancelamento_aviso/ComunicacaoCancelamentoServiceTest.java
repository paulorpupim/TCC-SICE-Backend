package com.tccds.sice.modules.cancelamento_aviso;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.exception.RegraNegocioEventoException;
import com.tccds.sice.modules.envio_aviso.EnvioAviso;
import com.tccds.sice.modules.envio_aviso.EnvioAvisoRepository;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.EventoRepository;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.modules.usuario.Usuario;

@ExtendWith(MockitoExtension.class)
class ComunicacaoCancelamentoServiceTest {
    @Mock EventoRepository eventos;
    @Mock EnvioAvisoRepository envios;
    @Mock ComunicacaoCancelamentoRepository comunicacoes;
    @Mock ApplicationEventPublisher publisher;

    private ComunicacaoCancelamentoService service;
    private Evento evento;
    private EnvioAviso original;

    @BeforeEach void preparar() {
        service = new ComunicacaoCancelamentoService(eventos, envios, comunicacoes, publisher);
        Usuario criador = new Usuario();
        criador.setId(8L);
        criador.setPerfil(PerfilUsuario.SECRETARIA);
        evento = new Evento("Feira escolar", "Descricao", LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(5),
                LocalTime.of(14, 0), criador);
        evento.setId(1L);
        GrupoWhatsapp grupo = new GrupoWhatsapp("Grupo de testes", "grupo-test@g.us", TipoGrupo.GERAL_ALUNOS, null);
        grupo.setId(2L);
        original = new EnvioAviso(evento, grupo);
        original.setId(3L);
        original.setStatus(StatusEnvioAviso.ENVIADO);
    }

    private void preparado(List<EnvioAviso> lista) {
        when(eventos.buscarParaCancelamento(1L)).thenReturn(Optional.of(evento));
        when(envios.findByEventoIdOrderByIdAsc(1L)).thenReturn(lista);
        for (EnvioAviso item : lista) {
            when(envios.buscarParaReserva(item.getId())).thenReturn(Optional.of(item));
        }
    }

    @Test void somenteAvisoOriginalConfirmadoGeraComunicacao() {
        preparado(List.of(original));
        var resultado = service.cancelarEComunicar(1L);
        assertEquals(StatusEvento.CANCELADO, evento.getStatus());
        assertEquals(1, resultado.comunicacoesCriadas());
        ArgumentCaptor<ComunicacaoCancelamento> captor = ArgumentCaptor.forClass(ComunicacaoCancelamento.class);
        verify(comunicacoes).save(captor.capture());
        assertEquals(3L, captor.getValue().getEnvioOriginalId());
        assertTrue(captor.getValue().getMensagem().contains("Feira escolar"));
        assertEquals("grupo-test@g.us", captor.getValue().getIdentificadorGrupo());
        verify(publisher).publishEvent(new CancelamentoComunicacaoEvent(1L));
    }

    @Test void avisoPendenteNaoGeraCancelamentoWhatsapp() {
        original.setStatus(StatusEnvioAviso.PENDENTE);
        preparado(List.of(original));
        var resultado = service.cancelarEComunicar(1L);
        assertEquals(0, resultado.comunicacoesCriadas());
        assertEquals(StatusEvento.CANCELADO, evento.getStatus());
        verify(comunicacoes, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test void grupoDesativadoNaoRecebeComunicacao() {
        original.getGrupoWhatsapp().setAtivo(false);
        preparado(List.of(original));
        var resultado = service.cancelarEComunicar(1L);
        assertEquals(0, resultado.comunicacoesCriadas());
        assertEquals(1, resultado.gruposIgnoradosInativos());
        verify(comunicacoes, never()).save(any());
    }

    @Test void avisoEmProcessamentoImpedeCancelamentoContraditorio() {
        original.setProcessandoDesde(java.time.LocalDateTime.now());
        preparado(List.of(original));
        assertThrows(RegraNegocioEventoException.class, () -> service.cancelarEComunicar(1L));
        assertEquals(StatusEvento.ATIVO, evento.getStatus());
        verify(comunicacoes, never()).save(any());
    }

    @Test void segundaRequisicaoNaoReenvia() {
        evento.setStatus(StatusEvento.CANCELADO);
        when(eventos.buscarParaCancelamento(1L)).thenReturn(Optional.of(evento));
        assertThrows(RegraNegocioEventoException.class, () -> service.cancelarEComunicar(1L));
        verifyNoInteractions(envios, publisher);
    }

    @Test void naoCriaComunicacaoParaEventoJaOcorrido() {
        evento.setDataInicio(LocalDate.now(ZoneId.of("America/Sao_Paulo")).minusDays(1));
        preparado(List.of(original));
        var resultado = service.cancelarEComunicar(1L);
        assertEquals(0, resultado.comunicacoesCriadas());
        assertEquals(StatusEvento.CANCELADO, evento.getStatus());
    }
}
