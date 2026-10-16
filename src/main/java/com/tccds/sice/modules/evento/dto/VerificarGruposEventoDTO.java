package com.tccds.sice.modules.evento.dto;

import java.util.Set;

import com.tccds.sice.modules.evento.evento_destino_turma.dto.CriarEventoDestinoDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record VerificarGruposEventoDTO(
    @NotEmpty @Valid Set<CriarEventoDestinoDTO> destinos
) {}
