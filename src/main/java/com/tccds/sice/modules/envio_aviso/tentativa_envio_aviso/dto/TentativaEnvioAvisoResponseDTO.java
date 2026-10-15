package com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.dto;

import java.time.LocalDateTime;

import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAviso;

public record TentativaEnvioAvisoResponseDTO(
        Long id,
        Integer numeroTentativa,
        LocalDateTime iniciadaEm,
        LocalDateTime finalizadaEm,
        ResultadoTentativaEnvioAviso resultado,
        String detalheErro
) {
    public TentativaEnvioAvisoResponseDTO(TentativaEnvioAviso tentativa) {
        this(tentativa.getId(), tentativa.getNumeroTentativa(),
                tentativa.getIniciadaEm(), tentativa.getFinalizadaEm(),
                tentativa.getResultado(), tentativa.getDetalheErro());
    }
}
