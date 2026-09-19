package com.tccds.sice.modules.evento.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.enums.PerfilUsuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarEventoDTO(

    @NotBlank
    @Size(max = 100)
    String titulo,

    @NotBlank
    String descricao,

    @NotNull
    LocalDateTime dataHoraInicio,

    @NotNull
    Set<PerfilUsuario> perfisDestinados,

    @NotNull
    Set<Etapa> etapasDestinadas,

    @NotNull
    Set<ModalidadeEnsino> modalidadesDestinadas,

    @NotNull
    Set<Long> turmasDestinadasIds

) {
    
}
