package com.tccds.sice.modules.curso.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarCursoDTO(

    @NotBlank
    String nome

) {
    
}
