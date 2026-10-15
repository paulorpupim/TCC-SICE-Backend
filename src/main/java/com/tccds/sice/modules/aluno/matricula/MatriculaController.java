package com.tccds.sice.modules.aluno.matricula;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.dto.AlterarStatusDTO;
import com.tccds.sice.modules.aluno.matricula.dto.CriarMatriculaDTO;
import com.tccds.sice.modules.aluno.matricula.dto.MatriculaResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios/{usuarioId}/matriculas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MatriculaController {

    private final MatriculaService matriculaService;

    @GetMapping
    public ResponseEntity<List<MatriculaResponseDTO>> listar(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(matriculaService.listarPorUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<MatriculaResponseDTO> adicionar(
            @PathVariable Long usuarioId, @Valid @RequestBody CriarMatriculaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matriculaService.adicionar(usuarioId, dto.turmaId()));
    }

    @PatchMapping("/{matriculaId}/status")
    public ResponseEntity<MatriculaResponseDTO> alterarStatus(
            @PathVariable Long usuarioId,
            @PathVariable Long matriculaId,
            @Valid @RequestBody AlterarStatusDTO dto) {
        return ResponseEntity.ok(matriculaService.alterarStatus(usuarioId, matriculaId, dto.ativo()));
    }
}
