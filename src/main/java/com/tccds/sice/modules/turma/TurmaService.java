package com.tccds.sice.modules.turma;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tccds.sice.modules.curso.Curso;
import com.tccds.sice.modules.curso.CursoService;
import com.tccds.sice.modules.turma.dto.CriarTurmaDTO;
import com.tccds.sice.modules.turma.dto.TurmaResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TurmaService {
    
    private final TurmaRepository turmaRepository;
    private final CursoService cursoService;

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

    public Turma buscarTurmaId(Long id){

        return turmaRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Turma não encontrada"));
    
    }

    public List<TurmaResponseDTO> listarTodos() {

        return turmaRepository.findAll()
                .stream()
                .map(TurmaResponseDTO::new)
                .toList();

    }

}
