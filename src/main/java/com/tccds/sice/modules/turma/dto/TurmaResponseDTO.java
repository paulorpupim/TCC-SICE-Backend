package com.tccds.sice.modules.turma.dto;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.modules.turma.Turma;

public record TurmaResponseDTO(

    Long id,
    Integer anoLetivo,
    Etapa etapa,
    ModalidadeEnsino modalidade,
    Long cursoId,
    String cursoNome,
    Boolean ativo

) {

    public TurmaResponseDTO(Turma turma){
        this(
            turma.getId(),
            turma.getAnoLetivo(),
            turma.getEtapa(),
            turma.getModalidade(),
            turma.getCurso().getId(),
            turma.getCurso().getNome(),
            turma.isAtivo()
        );
    }
    
}
