package com.tccds.sice.modules.grupo_whatsapp;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.dto.AlterarStatusDTO;
import com.tccds.sice.modules.grupo_whatsapp.dto.CriarGrupoWhatsappDTO;
import com.tccds.sice.modules.grupo_whatsapp.dto.EditarGrupoWhatsappDTO;
import com.tccds.sice.modules.grupo_whatsapp.dto.GrupoWhatsappResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/grupos-whatsapp")
@RequiredArgsConstructor
public class GrupoWhatsappController {

    private final GrupoWhatsappService grupoService;

    @GetMapping("/{id}")
    public ResponseEntity<GrupoWhatsappResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                grupoService.buscarGrupoPorId(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastrarGrupo")
    public ResponseEntity<GrupoWhatsappResponseDTO> cadastrar(
            @Valid @RequestBody CriarGrupoWhatsappDTO dto) {

        GrupoWhatsappResponseDTO grupo =
                grupoService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupo);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<GrupoWhatsappResponseDTO> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarGrupoWhatsappDTO dto) {

        return ResponseEntity.ok(
                grupoService.editar(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listarGrupos")
    public ResponseEntity<List<GrupoWhatsappResponseDTO>> listar() {

        return ResponseEntity.ok(
                grupoService.listarTodos());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestBody AlterarStatusDTO dados) {

        grupoService.alterarStatus(
                id,
                dados.ativo());

        return ResponseEntity.noContent().build();
    }
}