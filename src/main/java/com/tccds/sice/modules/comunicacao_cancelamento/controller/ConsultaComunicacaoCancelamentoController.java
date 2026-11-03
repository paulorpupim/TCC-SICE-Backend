package com.tccds.sice.modules.comunicacao_cancelamento.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.enums.StatusComunicacaoCancelamento;
import com.tccds.sice.modules.comunicacao_cancelamento.dto.ComunicacaoCancelamentoResponseDTO;
import com.tccds.sice.modules.comunicacao_cancelamento.dto.PaginaComunicacoesCancelamentoResponseDTO;
import com.tccds.sice.modules.comunicacao_cancelamento.service.ConsultaComunicacaoCancelamentoService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/** Consultas somente-leitura: nenhuma destas rotas envia comunicados. */
@RestController
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
public class ConsultaComunicacaoCancelamentoController {
    private final ConsultaComunicacaoCancelamentoService service;

    /** Mantem a consulta por evento usada anteriormente. */
    @GetMapping("/eventos/{eventoId}/comunicacoes-cancelamento")
    public List<ComunicacaoCancelamentoResponseDTO> listarPorEvento(@PathVariable Long eventoId) {
        return service.listar(eventoId);
    }

    /** Consulta somente uma comunicacao, incluindo mensagem e historico de tentativas. */
    @GetMapping("/avisos/cancelamentos/{id}")
    public ComunicacaoCancelamentoResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    /** Lista todos os comunicados, com filtros opcionais e paginacao. */
    @GetMapping("/avisos/cancelamentos")
    public PaginaComunicacoesCancelamentoResponseDTO listarTodos(
            @RequestParam(required = false) StatusComunicacaoCancelamento status,
            @RequestParam(required = false) @Size(max = 100) String busca,
            @RequestParam(required = false) @Min(1) Long eventoId,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int tamanho) {
        return service.listarTodos(status, busca, eventoId, pagina, tamanho);
    }
}
