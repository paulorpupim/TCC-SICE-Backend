package com.tccds.sice.modules.comunicacao_cancelamento.dto;

import java.time.LocalDateTime;

import com.tccds.sice.enums.StatusComunicacaoCancelamento;
import com.tccds.sice.modules.comunicacao_cancelamento.entity.ComunicacaoCancelamento;

/** Dados de identificacao: tentativas detalhadas ficam na consulta por ID. */
public record ResumoComunicacaoCancelamentoResponseDTO(
        Long id,
        Long eventoId,
        String eventoTitulo,
        Long grupoId,
        String grupoNome,
        StatusComunicacaoCancelamento status,
        int tentativas,
        LocalDateTime criadoEm) {

    public ResumoComunicacaoCancelamentoResponseDTO(ComunicacaoCancelamento comunicacao) {
        this(
                comunicacao.getId(),
                comunicacao.getEvento().getId(),
                comunicacao.getEvento().getTitulo(),
                comunicacao.getGrupoWhatsapp().getId(),
                comunicacao.getGrupoWhatsapp().getNome(),
                comunicacao.getStatus(),
                comunicacao.getTentativas(),
                comunicacao.getCriadoEm());
    }
}
