package com.tccds.sice.modules.evento.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tccds.sice.modules.z_enums.Serie;
import com.tccds.sice.modules.z_enums.StatusEvento;


public record CadastrarEventoDTO(

    String titulo,
    String descricao,
    LocalDateTime dataHoraInicio,
    StatusEvento status,
    List<Serie> serie

) {
    
}
