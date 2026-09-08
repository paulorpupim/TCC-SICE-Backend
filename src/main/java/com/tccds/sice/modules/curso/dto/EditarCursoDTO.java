package com.tccds.sice.modules.curso.dto;


import jakarta.validation.constraints.NotBlank;

public record EditarCursoDTO(

    @NotBlank 
    String nome

) {
    
}
