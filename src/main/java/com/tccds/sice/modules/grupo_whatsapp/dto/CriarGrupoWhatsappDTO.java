package com.tccds.sice.modules.grupo_whatsapp.dto;

import com.tccds.sice.enums.TipoGrupo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarGrupoWhatsappDTO(

    @NotBlank
    String nome,

    @NotBlank
    String identificador,

    @NotNull
    TipoGrupo tipo,

    Long turmaId

) {}