package com.tccds.sice.modules.aluno.matricula.dto;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.modules.aluno.matricula.Matricula;

public record MatriculaResponseDTO(
        Long id,
        Long turmaId,
        String cursoNome,
        Integer anoLetivo,
        Etapa etapa,
        ModalidadeEnsino modalidade,
        boolean ativo,
        boolean turmaAtiva,
        boolean cursoAtivo
) {
    public MatriculaResponseDTO(Matricula matricula) {
        this(
                matricula.getId(),
                matricula.getTurma().getId(),
                matricula.getTurma().getCurso().getNome(),
                matricula.getTurma().getAnoLetivo(),
                matricula.getTurma().getEtapa(),
                matricula.getTurma().getModalidade(),
                matricula.isAtivo(),
                matricula.getTurma().isAtivo(),
                matricula.getTurma().getCurso().isAtivo()
        );
    }
}
