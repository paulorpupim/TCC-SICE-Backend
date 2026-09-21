package com.tccds.sice.modules.usuario.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.aluno.matricula.Matricula;
import com.tccds.sice.modules.usuario.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    PerfilUsuario perfil,
    Set<Long> turmasIds,
    String identificador,
    boolean ativo
) {

    public UsuarioResponseDTO(Usuario usuario, Aluno aluno) {
        this(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil(),

            aluno == null
                ? Set.of()
                : aluno.getMatriculas()
                    .stream()
                    .filter(Matricula::isAtivo)
                    .map(matricula -> matricula.getTurma().getId())
                    .collect(Collectors.toSet()),

            usuario.getCredencial().getIdentificador(),
            usuario.getCredencial().isAtivo()
        );
    }
}