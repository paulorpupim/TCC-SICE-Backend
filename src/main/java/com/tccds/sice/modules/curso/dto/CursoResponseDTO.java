package com.tccds.sice.modules.curso.dto;

import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.modules.curso.Curso;

public record CursoResponseDTO (

    Long id,
    String nome,
    ModalidadeEnsino modalidade
    
) {

    public CursoResponseDTO(Curso curso){
        this(
            curso.getId(),
            curso.getNome(),
            curso.getModalidade()
        );
    }

}
