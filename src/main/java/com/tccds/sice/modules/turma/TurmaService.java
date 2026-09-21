package com.tccds.sice.modules.turma;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.modules.curso.Curso;
import com.tccds.sice.modules.curso.CursoService;
import com.tccds.sice.modules.turma.dto.CriarTurmaDTO;
import com.tccds.sice.modules.turma.dto.EditarTurmaDTO;
import com.tccds.sice.modules.turma.dto.TurmaResponseDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TurmaService {
    
    private final TurmaRepository turmaRepository;
    private final CursoService cursoService;

    public Turma buscarTurmaId(Long id){

        return turmaRepository.findById(id).orElseThrow(
            () -> new EntidadeNaoEncontradaException("Turma não encontrada"));
    
    }

    public TurmaResponseDTO buscarTurmaPorId(Long id){
        Turma turma = buscarTurmaId(id);

        return new TurmaResponseDTO(turma);
    }

    public TurmaResponseDTO criar(CriarTurmaDTO dto){

        Curso curso = cursoService.buscarCursoId(dto.cursoId());

        Turma turma = new Turma(
            dto.anoLetivo(),
            dto.etapa(),
            dto.modalidade(),
            curso
        );

        Turma turmaSalva = turmaRepository.save(turma);

        return new TurmaResponseDTO(turmaSalva);

    }

    @Transactional 
    public TurmaResponseDTO editar(Long id, EditarTurmaDTO dto){
        Turma turma = buscarTurmaId(id);
        Curso curso = cursoService.buscarCursoId(dto.cursoId());

        turma.setAnoLetivo(dto.anoLetivo());
        turma.setEtapa(dto.etapa());
        turma.setModalidade(dto.modalidade());
        turma.setCurso(curso);

        Turma turmaSalva = turmaRepository.save(turma);

        return new TurmaResponseDTO(turmaSalva);
    }


    public List<TurmaResponseDTO> listarTodos() {

        return turmaRepository.findAll()
                .stream()
                .map(TurmaResponseDTO::new)
                .toList();

    }

    
    public void alterarStatus(Long id, boolean ativo) {

        Turma turma = buscarTurmaId(id);
        turma.setAtivo(ativo);

        turmaRepository.save(turma);
    }

}
