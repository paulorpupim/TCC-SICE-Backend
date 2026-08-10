package com.tccds.sice.modules.usuario.dto;

import com.tccds.sice.modules.z_shared.enums.PerfilUsuario;
import com.tccds.sice.modules.z_shared.enums.Serie;

public record criarUsuarioDTO(
        String identificador,
        String senha,
        String nome,
        String email,
        PerfilUsuario perfil,
        Serie serie) {

}
