package com.tccds.sice.modules.comunicacao_cancelamento._tentativa_comunicacao_cancelamento.dto;

import java.time.LocalDateTime;

import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;
import com.tccds.sice.modules.bot_whatsapp.InterpretadorErroBot;
import com.tccds.sice.modules.bot_whatsapp.dto.DiagnosticoErroBotDTO;
import com.tccds.sice.modules.comunicacao_cancelamento._tentativa_comunicacao_cancelamento.TentativaComunicacaoCancelamento;

public record TentativaComunicacaoCancelamentoResponseDTO(
        int numero,
        LocalDateTime inicio,
        LocalDateTime fim,
        ResultadoTentativaEnvioAviso resultado,
        String erro,
        DiagnosticoErroBotDTO diagnosticoErro) {

    public TentativaComunicacaoCancelamentoResponseDTO(
            TentativaComunicacaoCancelamento tentativa) {
        this(
                tentativa.getNumeroTentativa(),
                tentativa.getIniciadaEm(),
                tentativa.getFinalizadaEm(),
                tentativa.getResultado(),
                tentativa.getDetalheErro(),
                InterpretadorErroBot.interpretar(
                        tentativa.getDetalheErro()));
    }
}
