package com.tccds.sice.modules.curso.dto;

import com.tccds.sice.modules.curso.Curso;

public record CursoResponseDTO (

    Long id,
    String nome,
    boolean ativo
    
) {

    public CursoResponseDTO(Curso curso){
        this(
            curso.getId(),
            curso.getNome(),
            curso.isAtivo()
        );
    }

}
