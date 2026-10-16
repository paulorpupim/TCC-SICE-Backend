package com.tccds.sice.modules.cancelamento_aviso;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.modules.cancelamento_aviso.dto.ComunicacaoCancelamentoResponseDTO;
import lombok.RequiredArgsConstructor;

@RestController @RequiredArgsConstructor
@RequestMapping("/eventos/{eventoId}/comunicacoes-cancelamento")
public class ConsultaComunicacaoCancelamentoController {
    private final ConsultaComunicacaoCancelamentoService service;

    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    @GetMapping
    public List<ComunicacaoCancelamentoResponseDTO> listar(@PathVariable Long eventoId) {
        return service.listar(eventoId);
    }
}
