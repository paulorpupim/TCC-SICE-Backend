package com.tccds.sice.modules.evento.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import com.tccds.sice.modules.evento.evento_destino_turma.dto.CriarEventoDestinoDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditarEventoDTO(
    
    @NotBlank
    @Size(max = 100)
    String titulo,

    @NotBlank
    String descricao,

    @NotNull
    LocalDate dataInicio,

    @NotNull 
    LocalTime horaInicio,

    @NotEmpty
    @Valid
    Set<CriarEventoDestinoDTO> destinos

) {
    
}
