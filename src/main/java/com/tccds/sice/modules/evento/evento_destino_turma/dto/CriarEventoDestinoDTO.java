package com.tccds.sice.modules.evento.evento_destino_turma.dto;

import java.util.Set;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.enums.PerfilUsuario;

import jakarta.validation.constraints.NotNull;

public record CriarEventoDestinoDTO(

    @NotNull 
    PerfilUsuario perfil,

    @NotNull
    Set<Etapa> etapas,

    @NotNull
    Set<ModalidadeEnsino> modalidades,

    @NotNull
    Set<Long> turmasIds

) {
    
}
