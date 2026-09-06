package com.tccds.sice.modules.curso;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tccds.sice.modules.curso.dto.CriarCursoDTO;
import com.tccds.sice.modules.curso.dto.CursoResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CursoService {
    
    private final CursoRepository cursoRepository;

    public CursoResponseDTO criar(CriarCursoDTO dto){

        Curso curso = new Curso(
            dto.nome()
        );

        Curso cursoSalvo = cursoRepository.save(curso);

        return new CursoResponseDTO(cursoSalvo);

    }

    public Curso buscarCursoId(Long id){
        return cursoRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Curso não encontrado"));
    }

    public List<CursoResponseDTO> listarTodos() {

        return cursoRepository.findAll()
                .stream()
                .map(CursoResponseDTO::new)
                .toList();
                
    }

}
