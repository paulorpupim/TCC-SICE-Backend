package com.tccds.sice.modules.grupo_whatsapp.dto;

import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;

public record GrupoWhatsappResponseDTO(
        Long id,
        String nome,
        String identificador,
        TipoGrupo tipo,
        Long turmaId,
        String turmaDescricao,
        boolean ativo
) {

    public GrupoWhatsappResponseDTO(GrupoWhatsapp grupo) {
        this(
                grupo.getId(),
                grupo.getNome(),
                grupo.getIdentificador(),
                grupo.getTipo(),

                grupo.getTurma() != null
                        ? grupo.getTurma().getId()
                        : null,

                grupo.getTurma() != null
                        ? grupo.getTurma().getCurso().getNome()
                            + " - "
                            + grupo.getTurma().getAnoLetivo()
                            + " - "
                            + grupo.getTurma().getEtapa()
                            + " - "
                            + grupo.getTurma().getModalidade()
                        : null,

                grupo.isAtivo()
        );
    }
}