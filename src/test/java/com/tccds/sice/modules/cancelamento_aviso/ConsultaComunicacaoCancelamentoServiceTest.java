package com.tccds.sice.modules.cancelamento_aviso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.tccds.sice.enums.StatusComunicacaoCancelamento;
import com.tccds.sice.modules.comunicacao_cancelamento._tentativa_comunicacao_cancelamento.TentativaComunicacaoCancelamentoRepository;
import com.tccds.sice.modules.comunicacao_cancelamento.entity.ComunicacaoCancelamento;
import com.tccds.sice.modules.comunicacao_cancelamento.repository.ComunicacaoCancelamentoRepository;
import com.tccds.sice.modules.comunicacao_cancelamento.service.ConsultaComunicacaoCancelamentoService;
import com.tccds.sice.modules.evento.EventoRepository;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.exception.EntidadeNaoEncontradaException;

@ExtendWith(MockitoExtension.class)
class ConsultaComunicacaoCancelamentoServiceTest {
    @Mock EventoRepository eventoRepository;
    @Mock ComunicacaoCancelamentoRepository repository;
    @Mock TentativaComunicacaoCancelamentoRepository tentativaRepository;
    private ConsultaComunicacaoCancelamentoService service;

    @BeforeEach
    void configurar() {
        service = new ConsultaComunicacaoCancelamentoService(eventoRepository, repository, tentativaRepository);
    }

    @Test
    void consultaGeralSemEventoRetornaPaginaVazia() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        var resposta = service.listarTodos(null, null, null, 0, 15);

        assertTrue(resposta.itens().isEmpty());
        assertEquals(0, resposta.total());
        assertEquals(0, resposta.totalPaginas());

        ArgumentCaptor<Pageable> paginacao = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), paginacao.capture());
        assertEquals(0, paginacao.getValue().getPageNumber());
        assertEquals(15, paginacao.getValue().getPageSize());
        assertEquals("id: DESC", paginacao.getValue().getSort().toString());
    }

    @Test
    void filtrosDeStatusBuscaEEventoUsamConsultaPaginada() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<ComunicacaoCancelamento>(List.of()));

        var resposta = service.listarTodos(StatusComunicacaoCancelamento.ERRO, "  Reunião  ", 20L, 1, 15);

        assertTrue(resposta.itens().isEmpty());
        ArgumentCaptor<Pageable> paginacao = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), paginacao.capture());
        assertEquals(1, paginacao.getValue().getPageNumber());
    }
    @Test
    void consultaGeralRetornaResumoSemBuscarTentativas() {
        ComunicacaoCancelamento comunicacao = mock(ComunicacaoCancelamento.class);
        Evento evento = mock(Evento.class);
        GrupoWhatsapp grupo = mock(GrupoWhatsapp.class);
        when(comunicacao.getId()).thenReturn(12L);
        when(comunicacao.getEvento()).thenReturn(evento);
        when(comunicacao.getGrupoWhatsapp()).thenReturn(grupo);
        when(evento.getTitulo()).thenReturn("Reunião de pais");
        when(grupo.getNome()).thenReturn("Terceiro ano");
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(comunicacao)));

        var resposta = service.listarTodos(null, null, null, 0, 15);

        assertEquals(1, resposta.itens().size());
        assertEquals(12L, resposta.itens().get(0).id());
        assertEquals("Reunião de pais", resposta.itens().get(0).eventoTitulo());
        verify(tentativaRepository, never()).findByComunicacaoIdOrderByNumeroTentativaAsc(any());
    }

    @Test
    void consultaDetalhesBuscaHistoricoDeUmaComunicacao() {
        ComunicacaoCancelamento comunicacao = mock(ComunicacaoCancelamento.class);
        Evento evento = mock(Evento.class);
        GrupoWhatsapp grupo = mock(GrupoWhatsapp.class);
        when(comunicacao.getId()).thenReturn(12L);
        when(comunicacao.getEvento()).thenReturn(evento);
        when(comunicacao.getGrupoWhatsapp()).thenReturn(grupo);
        when(repository.buscarDetalhesPorId(12L)).thenReturn(Optional.of(comunicacao));
        when(tentativaRepository.findByComunicacaoIdOrderByNumeroTentativaAsc(12L))
            .thenReturn(List.of());

        var resposta = service.buscarPorId(12L);

        assertEquals(12L, resposta.id());
        assertTrue(resposta.historico().isEmpty());
        verify(tentativaRepository).findByComunicacaoIdOrderByNumeroTentativaAsc(12L);
    }

    @Test
    void consultaDetalhesInexistentesRetornaErroDeEntidade() {
        when(repository.buscarDetalhesPorId(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.buscarPorId(999L));
    }
}
