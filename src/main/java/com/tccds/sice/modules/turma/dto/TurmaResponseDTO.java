package com.tccds.sice.modules.turma.dto;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.modules.turma.Turma;

public record TurmaResponseDTO(

    Long id,
    Integer anoLetivo,
    Etapa etapa,
    Long cursoId,
    Boolean ativo

) {

    public TurmaResponseDTO(Turma turma){
        this(
            turma.getId(),
            turma.getAnoLetivo(),
            turma.getEtapa(),
            turma.getCurso().getId(),
            turma.getAtivo()
        );
    }
    
}
