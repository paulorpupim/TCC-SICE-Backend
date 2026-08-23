package com.tccds.sice.modules.usuario.dto;

import java.util.Set;

import com.tccds.sice.enums.PerfilUsuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarUsuarioDTO(
        
        @NotBlank
        String identificador,
        
        @NotBlank
        String senha,
        
        @NotBlank
        @Size(max = 100)
        String nome,
        
        @NotBlank
        String email,
        
        @NotNull
        PerfilUsuario perfil,

        Set<Long> turmasIds
) {

}
