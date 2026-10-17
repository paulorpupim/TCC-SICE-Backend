package com.tccds.sice.modules.evento.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.evento_destino_turma.dto.EventoDestinoResponseDTO;

public record EventoResponseDTO(
        Long id,
        String titulo,
        String descricao,
        LocalDate dataInicio,
        LocalTime horaInicio,
        LocalDate dataAviso,
        StatusEvento status,
        Set<EventoDestinoResponseDTO> destinos,
        Long criadoPor,
        String criadoPorNome
) {

    public EventoResponseDTO(Evento evento) {
        this(
                evento.getId(),
                evento.getTitulo(),
                evento.getDescricao(),
                evento.getDataInicio(),
                evento.getHoraInicio(),
                evento.getDataAviso(),
                evento.getStatus(),
                evento.getDestinos()
                        .stream()
                        .map(EventoDestinoResponseDTO::new)
                        .collect(Collectors.toSet()),
                evento.getCriadoPor().getId(),
                evento.getCriadoPor().getNome()
        );
    }
}