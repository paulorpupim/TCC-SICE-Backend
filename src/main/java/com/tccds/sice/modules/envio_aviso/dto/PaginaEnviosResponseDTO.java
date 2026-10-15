package com.tccds.sice.modules.envio_aviso.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.tccds.sice.modules.envio_aviso.EnvioAviso;

/** Paginação com contrato estável para o React. */
public record PaginaEnviosResponseDTO(
        List<EnvioAvisoResponseDTO> itens,
        int pagina,
        int tamanho,
        long total,
        int totalPaginas
) {
    public PaginaEnviosResponseDTO(Page<EnvioAviso> resultado) {
        this(resultado.getContent().stream().map(EnvioAvisoResponseDTO::new).toList(),
                resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }
}
