package com.tccds.sice.modules.usuario.dto;

import com.tccds.sice.modules.z_enums.PerfilUsuario;
import com.tccds.sice.modules.z_enums.Serie;

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

        @NotNull
        Serie serie) {

}
