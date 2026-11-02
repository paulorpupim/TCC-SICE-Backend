package com.tccds.sice.modules.envio_aviso._tentativa_envio_aviso.dto;

import java.time.LocalDateTime;

import com.tccds.sice.modules.bot_whatsapp.InterpretadorErroBot;
import com.tccds.sice.modules.bot_whatsapp.dto.DiagnosticoErroBotDTO;
import com.tccds.sice.modules.envio_aviso._tentativa_envio_aviso.TentativaEnvioAviso;
import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;

public record TentativaEnvioAvisoResponseDTO(
        Long id,
        Integer numeroTentativa,
        LocalDateTime iniciadaEm,
        LocalDateTime finalizadaEm,
        ResultadoTentativaEnvioAviso resultado,
        String detalheErro,
        DiagnosticoErroBotDTO diagnosticoErro
) {
    public TentativaEnvioAvisoResponseDTO(TentativaEnvioAviso tentativa) {
        this(tentativa.getId(), tentativa.getNumeroTentativa(),
                tentativa.getIniciadaEm(), tentativa.getFinalizadaEm(),
                tentativa.getResultado(), tentativa.getDetalheErro(),
                InterpretadorErroBot.interpretar(tentativa.getDetalheErro()));
    }
}
