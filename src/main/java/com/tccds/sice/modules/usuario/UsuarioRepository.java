package com.tccds.sice.modules.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tccds.sice.enums.PerfilUsuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCredencial_Identificador(String identificador);

    List<Usuario> findByPerfil (PerfilUsuario perfil);
    
}
