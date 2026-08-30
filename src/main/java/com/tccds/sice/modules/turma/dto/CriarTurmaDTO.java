package com.tccds.sice.modules.turma.dto;

import com.tccds.sice.enums.Etapa;
import jakarta.validation.constraints.NotNull;

public record CriarTurmaDTO(

    @NotNull
    Integer anoLetivo,

    @NotNull
    Etapa etapa,

    @NotNull
    Long cursoId

) {
    
}
