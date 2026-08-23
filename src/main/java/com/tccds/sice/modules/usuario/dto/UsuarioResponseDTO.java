package com.tccds.sice.modules.usuario.dto;

import java.util.Set;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.usuario.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    PerfilUsuario perfil,
    Set<Long> turmasIds,
    String identificador
) {
    public UsuarioResponseDTO(Usuario usuario, Set<Long> turmasIds){
        this(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil(),
            turmasIds,
            usuario.getCredencial().getIdentificador()
        );
    }
}
