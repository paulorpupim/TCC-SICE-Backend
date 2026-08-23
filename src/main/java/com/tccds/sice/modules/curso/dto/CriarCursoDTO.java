package com.tccds.sice.modules.curso.dto;

import com.tccds.sice.enums.ModalidadeEnsino;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarCursoDTO(

    @NotBlank
    String nome,

    @NotNull
    ModalidadeEnsino modalidade

) {
    
}
