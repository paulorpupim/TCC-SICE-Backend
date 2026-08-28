package com.tccds.sice.modules.aluno;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tccds.sice.modules.usuario.Usuario;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByUsuario(Usuario usuario);
    
}
