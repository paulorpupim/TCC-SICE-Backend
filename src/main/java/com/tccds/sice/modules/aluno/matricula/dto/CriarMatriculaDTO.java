package com.tccds.sice.modules.aluno.matricula.dto;

import jakarta.validation.constraints.NotNull;

public record CriarMatriculaDTO(
    @NotNull
    Long turmaId
) {
    
}
