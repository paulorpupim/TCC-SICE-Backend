package com.tccds.sice.modules.envio_aviso;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.modules.envio_aviso.dto.EnvioAvisoResponseDTO;
import com.tccds.sice.modules.envio_aviso.dto.PaginaEnviosResponseDTO;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.dto.TentativaEnvioAvisoResponseDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/** Acesso administrativo somente-leitura, sem endpoints de disparo ou reenvio. */
@RestController
@RequestMapping("/avisos/envios")
@PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIA')")
@Validated
@RequiredArgsConstructor
public class ConsultaEnvioAvisoController {

    private final ConsultaEnvioAvisoService service;

    @GetMapping
    public PaginaEnviosResponseDTO listar(
            @RequestParam(required = false) StatusEnvioAviso status,
            @RequestParam(required = false) @Size(max = 100) String busca,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int tamanho) {
        return service.listar(status, busca, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public EnvioAvisoResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
    @GetMapping("/{id}/tentativas")
    public List<TentativaEnvioAvisoResponseDTO> listarTentativas(@PathVariable Long id) {
        return service.listarTentativas(id);
    }
}
