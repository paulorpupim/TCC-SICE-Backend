package com.tccds.sice.modules.usuario.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditarUsuarioDTO(

        @NotBlank
        String identificador,
        
        @NotBlank
        @Size(max = 100)
        String nome,
        
        @NotBlank
        String email,

        @NotNull
        Set<Long> turmasIds
        
) {
    
}
