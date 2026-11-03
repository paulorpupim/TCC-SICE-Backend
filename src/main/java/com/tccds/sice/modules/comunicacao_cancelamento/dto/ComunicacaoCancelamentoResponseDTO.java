package com.tccds.sice.modules.comunicacao_cancelamento.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tccds.sice.modules.bot_whatsapp.InterpretadorErroBot;
import com.tccds.sice.modules.bot_whatsapp.dto.DiagnosticoErroBotDTO;
import com.tccds.sice.modules.comunicacao_cancelamento._tentativa_comunicacao_cancelamento.TentativaComunicacaoCancelamento;
import com.tccds.sice.modules.comunicacao_cancelamento._tentativa_comunicacao_cancelamento.dto.TentativaComunicacaoCancelamentoResponseDTO;
import com.tccds.sice.modules.comunicacao_cancelamento.entity.ComunicacaoCancelamento;
import com.tccds.sice.enums.StatusComunicacaoCancelamento;


public record ComunicacaoCancelamentoResponseDTO(
        Long id,
        Long eventoId,
        String eventoTitulo,
        Long grupoId,
        String grupoNome,
        Long envioOriginalId,
        String identificadorGrupo,
        String mensagem,
        StatusComunicacaoCancelamento status,
        int tentativas,
        LocalDateTime criadoEm,
        LocalDateTime enviadoEm,
        String ultimoErro,
        DiagnosticoErroBotDTO diagnosticoErro,
        List<TentativaComunicacaoCancelamentoResponseDTO> historico) {

    public ComunicacaoCancelamentoResponseDTO(
            ComunicacaoCancelamento comunicacao,
            List<TentativaComunicacaoCancelamento> tentativas) {
        this(
                comunicacao.getId(),
                comunicacao.getEvento().getId(),
                comunicacao.getEvento().getTitulo(),
                comunicacao.getGrupoWhatsapp().getId(),
                comunicacao.getGrupoWhatsapp().getNome(),
                comunicacao.getEnvioOriginalId(),
                comunicacao.getIdentificadorGrupo(),
                comunicacao.getMensagem(),
                comunicacao.getStatus(),
                comunicacao.getTentativas(),
                comunicacao.getCriadoEm(),
                comunicacao.getEnviadoEm(),
                comunicacao.getUltimoErro(),
                InterpretadorErroBot.interpretar(
                        comunicacao.getUltimoErro()),
                tentativas
                        .stream()
                        .map(TentativaComunicacaoCancelamentoResponseDTO::new)
                        .toList());
    }
}
