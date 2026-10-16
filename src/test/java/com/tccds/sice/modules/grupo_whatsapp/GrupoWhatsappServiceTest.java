package com.tccds.sice.modules.grupo_whatsapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.exception.RegraNegocioCadastroException;
import com.tccds.sice.modules.curso.Curso;
import com.tccds.sice.modules.grupo_whatsapp.dto.CriarGrupoWhatsappDTO;
import com.tccds.sice.modules.grupo_whatsapp.dto.EditarGrupoWhatsappDTO;
import com.tccds.sice.modules.turma.Turma;
import com.tccds.sice.modules.turma.TurmaService;

@ExtendWith(MockitoExtension.class)
class GrupoWhatsappServiceTest {
    @Mock GrupoWhatsappRepository repository;
    @Mock TurmaService turmaService;
    private GrupoWhatsappService service;

    @BeforeEach
    void configurar() {
        service = new GrupoWhatsappService(repository, turmaService);
    }

    @Test
    void impedeIdentificadorDuplicadoNoCadastro() {
        when(repository.existsByIdentificador("repetido@g.us")).thenReturn(true);
        var dto = new CriarGrupoWhatsappDTO("Grupo", "repetido@g.us", TipoGrupo.GERAL_ALUNOS, null);
        assertThrows(RegraNegocioCadastroException.class, () -> service.criar(dto));
        verify(repository, never()).save(any(GrupoWhatsapp.class));
    }

    @Test
    void grupoDeTurmaExigeIdDeTurma() {
        var dto = new CriarGrupoWhatsappDTO("Grupo", "turma@g.us", TipoGrupo.TURMA, null);
        assertThrows(RegraNegocioCadastroException.class, () -> service.criar(dto));
    }

    @Test
    void grupoGeralNaoPodeApontarParaTurma() {
        var dto = new CriarGrupoWhatsappDTO("Grupo", "geral@g.us", TipoGrupo.GERAL_ALUNOS, 5L);
        assertThrows(RegraNegocioCadastroException.class, () -> service.criar(dto));
    }

    @Test
    void impedeDoisGruposParaMesmaTurma() {
        when(repository.existsByTurmaId(5L)).thenReturn(true);
        var dto = new CriarGrupoWhatsappDTO("Grupo", "turma@g.us", TipoGrupo.TURMA, 5L);
        assertThrows(RegraNegocioCadastroException.class, () -> service.criar(dto));
    }

    @Test
    void permiteCadastrarGrupoValidoParaTurma() {
        Turma turma = new Turma(2026, Etapa.PRIMEIRA, ModalidadeEnsino.TECNICO, new Curso("DS"));
        turma.setId(5L);
        when(turmaService.buscarTurmaId(5L)).thenReturn(turma);
        when(repository.save(any(GrupoWhatsapp.class))).thenAnswer(inv -> inv.getArgument(0));
        var dto = new CriarGrupoWhatsappDTO("Grupo", "turma@g.us", TipoGrupo.TURMA, 5L);
        var resposta = service.criar(dto);
        assertEquals("Grupo", resposta.nome());
        verify(repository).save(any(GrupoWhatsapp.class));
    }

    @Test
    void impedeIdentificadorDeOutroGrupoNaEdicao() {
        GrupoWhatsapp grupo = new GrupoWhatsapp("Antigo", "antigo@g.us", TipoGrupo.GERAL_ALUNOS, null);
        grupo.setId(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(grupo));
        when(repository.existsByIdentificadorAndIdNot("existente@g.us", 10L)).thenReturn(true);
        var dto = new EditarGrupoWhatsappDTO("Novo", "existente@g.us", TipoGrupo.GERAL_ALUNOS, null);
        assertThrows(RegraNegocioCadastroException.class, () -> service.editar(10L, dto));
    }

    @Test
    void alterarStatusPersisteDesativacao() {
        GrupoWhatsapp grupo = new GrupoWhatsapp("Grupo", "grupo@g.us", TipoGrupo.GERAL_ALUNOS, null);
        grupo.setId(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(grupo));
        service.alterarStatus(10L, false);
        assertFalse(grupo.isAtivo());
        verify(repository).save(grupo);
    }
}
