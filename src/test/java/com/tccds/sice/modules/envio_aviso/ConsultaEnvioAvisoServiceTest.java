package com.tccds.sice.modules.envio_aviso;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAvisoRepository;

@ExtendWith(MockitoExtension.class)
class ConsultaEnvioAvisoServiceTest {
    @Mock EnvioAvisoRepository repository;
    @Mock TentativaEnvioAvisoRepository tentativaRepository;
    private ConsultaEnvioAvisoService service;

    @BeforeEach
    void configurar() {
        service = new ConsultaEnvioAvisoService(repository, tentativaRepository);
    }

    @Test
    void semFiltrosSelecionaConsultaGeral() {
        when(repository.consultarTodos(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        var resultado = service.listar(null, null, 0, 15);
        assertTrue(resultado.itens().isEmpty());
        verify(repository).consultarTodos(any(Pageable.class));
        verify(repository, never()).consultarPorBusca(anyString(), any(Pageable.class));
    }

    @Test
    void somenteStatusUsaConsultaSemBuscaNula() {
        when(repository.consultarPorStatus(eq(StatusEnvioAviso.ERRO), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        service.listar(StatusEnvioAviso.ERRO, "   ", 0, 15);
        verify(repository).consultarPorStatus(eq(StatusEnvioAviso.ERRO), any(Pageable.class));
    }

    @Test
    void textoSozinhoETrimadoENormalizado() {
        when(repository.consultarPorBusca(eq("%reuniao%"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        service.listar(null, "  REUNIAO  ", 0, 15);
        verify(repository).consultarPorBusca(eq("%reuniao%"), any(Pageable.class));
    }

    @Test
    void statusMaisTextoSelecionaConsultaCombinada() {
        when(repository.consultarPorStatusEBusca(eq(StatusEnvioAviso.PENDENTE),
                eq("%grupo%"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        service.listar(StatusEnvioAviso.PENDENTE, "Grupo", 0, 15);
        verify(repository).consultarPorStatusEBusca(eq(StatusEnvioAviso.PENDENTE),
                eq("%grupo%"), any(Pageable.class));
    }

    @Test
    void idInexistenteGeraNaoEncontrado() {
        when(repository.consultarPorId(404L)).thenReturn(Optional.empty());
        assertThrows(EntidadeNaoEncontradaException.class, () -> service.buscarPorId(404L));
    }

    @Test
    void tentativasDeEnvioInexistenteNaoSaoExpostas() {
        when(repository.existsById(404L)).thenReturn(false);
        assertThrows(EntidadeNaoEncontradaException.class, () -> service.listarTentativas(404L));
        verifyNoInteractions(tentativaRepository);
    }

    @Test
    void envioSemTentativasRetornaListaVazia() {
        when(repository.existsById(9L)).thenReturn(true);
        when(tentativaRepository.findByEnvioAvisoIdOrderByNumeroTentativaAsc(9L))
                .thenReturn(List.of());
        assertTrue(service.listarTentativas(9L).isEmpty());
    }
}
