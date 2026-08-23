package com.tccds.sice.modules.turma.dto;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.modules.turma.Turma;

public record TurmaResponseDTO(

    Long id,
    Etapa etapa,
    Long cursoId

) {

    public TurmaResponseDTO(Turma turma){
        this(
            turma.getId(),
            turma.getEtapa(),
            turma.getCurso().getId()
        );
    }
    
}
