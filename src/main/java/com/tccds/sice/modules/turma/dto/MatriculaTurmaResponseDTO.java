package com.tccds.sice.modules.turma.dto;

import com.tccds.sice.modules.aluno.matricula.Matricula;

public record MatriculaTurmaResponseDTO(
        Long id,
        Long usuarioId,
        String nome,
        String email,
        String identificador,
        boolean usuarioAtivo,
        boolean ativo
) {
    public MatriculaTurmaResponseDTO(Matricula matricula) {
        this(
                matricula.getId(),
                matricula.getAluno().getUsuario().getId(),
                matricula.getAluno().getUsuario().getNome(),
                matricula.getAluno().getUsuario().getEmail(),
                matricula.getAluno().getUsuario().getCredencial().getIdentificador(),
                matricula.getAluno().getUsuario().getCredencial().isAtivo(),
                matricula.isAtivo()
        );
    }
}
