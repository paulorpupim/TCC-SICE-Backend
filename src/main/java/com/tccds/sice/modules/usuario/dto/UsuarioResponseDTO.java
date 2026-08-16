package com.tccds.sice.modules.usuario.dto;

import com.tccds.sice.modules.z_enums.PerfilUsuario;
import com.tccds.sice.modules.z_enums.Serie;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    PerfilUsuario perfil,
    Serie serie,
    String identificador
) {
    
}
