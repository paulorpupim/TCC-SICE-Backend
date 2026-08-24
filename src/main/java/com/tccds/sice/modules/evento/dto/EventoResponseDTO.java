package com.tccds.sice.modules.evento.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.evento.Evento;

public record EventoResponseDTO(
    Long id,
    String titulo,
    String descricao,
    LocalDateTime dataHoraInicio,
    StatusEvento status,
    Set<PerfilUsuario> perfisDestinados,
    Set<Etapa> etapasDestinadas,
    Set<ModalidadeEnsino> modalidadesDestinadas,
    Set<Long> turmasDestinadasIds,
    Long criadoPor
) {

    public EventoResponseDTO(Evento evento, Set<Long> turmasDestinadasIds) {
        this(
            evento.getId(),
            evento.getTitulo(),
            evento.getDescricao(),
            evento.getDataHoraInicio(),
            evento.getStatus(),
            evento.getPerfisDestinados(),
            evento.getEtapasDestinadas(),
            evento.getModalidadesDestinadas(),
            turmasDestinadasIds,
            evento.getCriadoPor().getId()
        );
    }
}