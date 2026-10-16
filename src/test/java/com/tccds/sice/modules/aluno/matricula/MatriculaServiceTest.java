package com.tccds.sice.modules.aluno.matricula;

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
import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.exception.RegraNegocioCadastroException;
import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.aluno.AlunoRepository;
import com.tccds.sice.modules.curso.Curso;
import com.tccds.sice.modules.turma.Turma;
import com.tccds.sice.modules.turma.TurmaService;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {
    @Mock MatriculaRepository matriculas;
    @Mock AlunoRepository alunos;
    @Mock TurmaService turmas;
    private MatriculaService service;
    private Aluno aluno;
    private Turma turma;
    private Matricula matricula;

    @BeforeEach
    void configurar() {
        service = new MatriculaService(matriculas, alunos, turmas);
        aluno = new Aluno();
        aluno.setId(10L);
        Curso curso = new Curso("Desenvolvimento de Sistemas");
        curso.setId(2L);
        turma = new Turma(2026, Etapa.PRIMEIRA, ModalidadeEnsino.TECNICO, curso);
        turma.setId(30L);
        matricula = new Matricula(aluno, turma, true);
        matricula.setId(40L);
    }

    private void alunoEncontrado() {
        when(alunos.findByUsuario_Id(1L)).thenReturn(Optional.of(aluno));
    }

    @Test
    void permiteDesativarUltimaMatriculaAtiva() {
        alunoEncontrado();
        when(matriculas.findById(40L)).thenReturn(Optional.of(matricula));
        var resultado = service.alterarStatus(1L, 40L, false);
        assertFalse(matricula.isAtivo());
        assertFalse(resultado.ativo());
    }

    @Test
    void permiteReativarQuandoTurmaECursoEstaoAtivos() {
        matricula.setAtivo(false);
        alunoEncontrado();
        when(matriculas.findById(40L)).thenReturn(Optional.of(matricula));
        var resultado = service.alterarStatus(1L, 40L, true);
        assertTrue(resultado.ativo());
    }

    @Test
    void naoReativaSeTurmaEstiverInativa() {
        matricula.setAtivo(false);
        turma.setAtivo(false);
        alunoEncontrado();
        when(matriculas.findById(40L)).thenReturn(Optional.of(matricula));
        assertThrows(RegraNegocioCadastroException.class,
                () -> service.alterarStatus(1L, 40L, true));
        assertFalse(matricula.isAtivo());
    }

    @Test
    void naoReativaSeCursoEstiverInativo() {
        matricula.setAtivo(false);
        turma.getCurso().setAtivo(false);
        alunoEncontrado();
        when(matriculas.findById(40L)).thenReturn(Optional.of(matricula));
        assertThrows(RegraNegocioCadastroException.class,
                () -> service.alterarStatus(1L, 40L, true));
    }

    @Test
    void naoAlteraMatriculaDeOutroAluno() {
        Aluno outroAluno = new Aluno();
        outroAluno.setId(999L);
        matricula.setAluno(outroAluno);
        alunoEncontrado();
        when(matriculas.findById(40L)).thenReturn(Optional.of(matricula));
        assertThrows(EntidadeNaoEncontradaException.class,
                () -> service.alterarStatus(1L, 40L, false));
    }

    @Test
    void impedeMatriculaDuplicada() {
        alunoEncontrado();
        when(matriculas.existsByAluno_IdAndTurma_Id(10L, 30L)).thenReturn(true);
        assertThrows(RegraNegocioCadastroException.class,
                () -> service.adicionar(1L, 30L));
        verify(matriculas, never()).save(any(Matricula.class));
    }

    @Test
    void permiteAdicionarMatriculaNovaEmTurmaAtiva() {
        alunoEncontrado();
        when(turmas.buscarTurmaId(30L)).thenReturn(turma);
        when(matriculas.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));
        var resultado = service.adicionar(1L, 30L);
        assertTrue(resultado.ativo());
        assertEquals(30L, resultado.turmaId());
        verify(matriculas).save(any(Matricula.class));
    }

    @Test
    void bloqueiaNovaMatriculaEmCursoInativo() {
        alunoEncontrado();
        turma.getCurso().setAtivo(false);
        when(turmas.buscarTurmaId(30L)).thenReturn(turma);
        assertThrows(RegraNegocioCadastroException.class,
                () -> service.adicionar(1L, 30L));
        verify(matriculas, never()).save(any(Matricula.class));
    }
}
