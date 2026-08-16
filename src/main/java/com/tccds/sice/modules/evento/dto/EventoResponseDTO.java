package com.tccds.sice.modules.evento.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.tccds.sice.modules.z_enums.PerfilUsuario;
import com.tccds.sice.modules.z_enums.Serie;

public record EventoResponseDTO (
    Long id,
    String titulo,
    String descricao,
    LocalDateTime dataHoraInicio,
    Set<Serie> seriesDestinadas,
    Set<PerfilUsuario> perfisDestinados
) {
    
}
