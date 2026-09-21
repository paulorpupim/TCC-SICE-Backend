package com.tccds.sice.modules.turma.dto;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;

import jakarta.validation.constraints.NotNull;

public record EditarTurmaDTO(
    @NotNull
    Integer anoLetivo,

    @NotNull
    Etapa etapa,

    @NotNull
    ModalidadeEnsino modalidade,

    @NotNull
    Long cursoId

) {
    
}
