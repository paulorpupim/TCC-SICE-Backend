package com.tccds.sice.modules.aluno.matricula;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.exception.RegraNegocioCadastroException;
import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.aluno.AlunoRepository;
import com.tccds.sice.modules.aluno.matricula.dto.MatriculaResponseDTO;
import com.tccds.sice.modules.turma.Turma;
import com.tccds.sice.modules.turma.TurmaService;
import com.tccds.sice.modules.turma.dto.MatriculaTurmaResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaService turmaService;

    public Matricula criar(Aluno aluno, Turma turma) {
        return matriculaRepository.save(new Matricula(aluno, turma, true));
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listarPorUsuario(Long usuarioId) {
        Aluno aluno = buscarAlunoPorUsuario(usuarioId);
        return matriculaRepository.listarPorAluno(aluno.getId()).stream()
                .map(MatriculaResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MatriculaTurmaResponseDTO> listarPorTurma(Long turmaId) {

        turmaService.buscarTurmaId(turmaId);
        return matriculaRepository.listarPorTurma(turmaId).stream()
                .map(MatriculaTurmaResponseDTO::new)
                .toList();
    }

    @Transactional
    public MatriculaResponseDTO adicionar(Long usuarioId, Long turmaId) {
        Aluno aluno = buscarAlunoPorUsuario(usuarioId);
        if (matriculaRepository.existsByAluno_IdAndTurma_Id(aluno.getId(), turmaId)) {
            throw new RegraNegocioCadastroException(
                    "O aluno já possui matrícula nessa turma. Use Ativar na tabela de matrículas.");
        }

        Turma turma = turmaService.buscarTurmaId(turmaId);
        validarTurmaAtiva(turma);
        return new MatriculaResponseDTO(criar(aluno, turma));
    }

    @Transactional
    public MatriculaResponseDTO alterarStatus(Long usuarioId, Long matriculaId, boolean ativo) {
        Aluno aluno = buscarAlunoPorUsuario(usuarioId);
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Matrícula não encontrada."));

        if (!matricula.getAluno().getId().equals(aluno.getId())) {
            throw new EntidadeNaoEncontradaException("Matrícula não encontrada para este aluno.");
        }
        if (matricula.isAtivo() == ativo) {
            return new MatriculaResponseDTO(matricula);
        }

        if (ativo) {
            validarTurmaAtiva(matricula.getTurma());
        }

        matricula.setAtivo(ativo);
        return new MatriculaResponseDTO(matricula);
    }

    private Aluno buscarAlunoPorUsuario(Long usuarioId) {
        return alunoRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado para este usuário."));
    }

    private void validarTurmaAtiva(Turma turma) {
        if (!turma.isAtivo() || !turma.getCurso().isAtivo()) {
            throw new RegraNegocioCadastroException(
                    "A turma e seu curso precisam estar ativos para receber uma matrícula.");
        }
    }
}
