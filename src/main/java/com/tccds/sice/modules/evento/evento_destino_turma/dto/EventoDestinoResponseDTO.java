package com.tccds.sice.modules.evento.evento_destino_turma.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.evento.evento_destino.EventoDestino;

public record EventoDestinoResponseDTO(
    Long id,
    PerfilUsuario perfil,
    Set<Etapa> etapas,
    Set<ModalidadeEnsino> modalidades,
    Set<Long> turmasIds
) {

    public EventoDestinoResponseDTO(EventoDestino destino) {
        this(
            destino.getId(),
            destino.getPerfil(),
            destino.getEtapas(),
            destino.getModalidades(),
            destino.getDestinacoesTurma()
                .stream()
                .map(destinacao -> destinacao.getTurma().getId())
                .collect(Collectors.toSet())
        );
    }
}