package com.tccds.sice.modules.comunicacao_cancelamento.dto;

import java.util.List;

/** Mesmo formato de paginacao utilizado pela consulta de envios registrados. */
public record PaginaComunicacoesCancelamentoResponseDTO(
        List<ResumoComunicacaoCancelamentoResponseDTO> itens,
        int pagina,
        int tamanho,
        long total,
        int totalPaginas
) {}
