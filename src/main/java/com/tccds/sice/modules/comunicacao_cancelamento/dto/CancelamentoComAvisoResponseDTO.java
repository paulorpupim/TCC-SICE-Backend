package com.tccds.sice.modules.comunicacao_cancelamento.dto;

import com.tccds.sice.modules.evento.dto.EventoResponseDTO;

public record CancelamentoComAvisoResponseDTO(
    EventoResponseDTO evento,
    int comunicacoesCriadas,
    int gruposIgnoradosInativos
) {
    
}
