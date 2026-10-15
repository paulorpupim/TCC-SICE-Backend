package com.tccds.sice.modules.aluno.matricula;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.exception.RegraNegocioCadastroException;
import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.aluno.AlunoRepository;
import com.tccds.sice.modules.curso.Curso;
import com.tccds.sice.modules.turma.Turma;
import com.tccds.sice.modules.turma.TurmaService;

class MatriculaServiceTest {

    private final MatriculaRepository repository = mock(MatriculaRepository.class);
    private final AlunoRepository alunoRepository = mock(AlunoRepository.class);
    private final TurmaService turmaService = mock(TurmaService.class);
    private final MatriculaService service = new MatriculaService(repository, alunoRepository, turmaService);
    private Aluno aluno;

    @BeforeEach
    void preparar() {
        aluno = new Aluno();
        aluno.setId(10L);
        when(alunoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(aluno));
    }

    @Test
    void listaMatriculasAtivasEInativasSemFiltrarHistorico() {
        Matricula ativa = matricula(20L, true, aluno);
        Matricula inativa = matricula(21L, false, aluno);
        when(repository.listarPorAluno(10L)).thenReturn(List.of(ativa, inativa));
        var resultado = service.listarPorUsuario(1L);
        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).ativo());
        assertFalse(resultado.get(1).ativo());
    }

    @Test
    void impedeDuplicarMatriculaMesmoQuandoAnteriorEstaInativa() {
        when(repository.existsByAluno_IdAndTurma_Id(10L, 5L)).thenReturn(true);
        assertThrows(RegraNegocioCadastroException.class, () -> service.adicionar(1L, 5L));
        verify(repository, never()).save(any());
    }

    @Test
    void impedeNovaMatriculaEmTurmaInativa() {
        Turma turma = turma();
        turma.setAtivo(false);
        when(repository.existsByAluno_IdAndTurma_Id(10L, 5L)).thenReturn(false);
        when(turmaService.buscarTurmaId(5L)).thenReturn(turma);
        assertThrows(RegraNegocioCadastroException.class, () -> service.adicionar(1L, 5L));
        verify(repository, never()).save(any());
    }

    @Test
    void impedeDesativarUltimaMatriculaAtiva() {
        when(repository.findById(20L)).thenReturn(Optional.of(matricula(20L, true, aluno)));
        when(repository.countByAluno_IdAndAtivoTrue(10L)).thenReturn(1L);
        assertThrows(RegraNegocioCadastroException.class,
                () -> service.alterarStatus(1L, 20L, false));
    }

    @Test
    void naoAlteraMatriculaDeOutroAluno() {
        Aluno outro = new Aluno();
        outro.setId(11L);
        when(repository.findById(20L)).thenReturn(Optional.of(matricula(20L, true, outro)));
        assertThrows(EntidadeNaoEncontradaException.class,
                () -> service.alterarStatus(1L, 20L, false));
        verify(repository, never()).countByAluno_IdAndAtivoTrue(any());
    }

    @Test
    void permiteReativarMatriculaDeTurmaAtiva() {
        Matricula matricula = matricula(20L, false, aluno);
        when(repository.findById(20L)).thenReturn(Optional.of(matricula));
        var resposta = service.alterarStatus(1L, 20L, true);
        assertTrue(resposta.ativo());
        assertTrue(matricula.isAtivo());
    }

    private Matricula matricula(Long id, boolean ativo, Aluno aluno) {
        Matricula m = new Matricula(aluno, turma(), ativo);
        m.setId(id);
        return m;
    }

    private Turma turma() {
        Curso curso = new Curso("Informática");
        curso.setId(3L);
        Turma t = new Turma();
        t.setId(5L);
        t.setAtivo(true);
        t.setCurso(curso);
        t.setAnoLetivo(2026);
        return t;
    }
}
