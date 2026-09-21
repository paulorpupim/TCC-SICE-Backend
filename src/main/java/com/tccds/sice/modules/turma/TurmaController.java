package com.tccds.sice.modules.turma;

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

import com.tccds.sice.modules.dto.AlterarStatusDTO;
import com.tccds.sice.modules.turma.dto.CriarTurmaDTO;
import com.tccds.sice.modules.turma.dto.EditarTurmaDTO;
import com.tccds.sice.modules.turma.dto.TurmaResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/turmas")
@RequiredArgsConstructor
public class TurmaController {

    private final TurmaService turmaService;

    @GetMapping("/{id}")
    public ResponseEntity<TurmaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(turmaService.buscarTurmaPorId(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastrarTurma")
    public ResponseEntity<TurmaResponseDTO> cadastrar(
            @Valid @RequestBody CriarTurmaDTO dto) {

        TurmaResponseDTO turma = turmaService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(turma);

    }

    @PutMapping("/{id}")
    public ResponseEntity<TurmaResponseDTO> editar(
        @PathVariable Long id, @RequestBody EditarTurmaDTO dto){
            return ResponseEntity.ok(turmaService.editar(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listarTurmas")
    public ResponseEntity<List<TurmaResponseDTO>> listar() {
        return ResponseEntity.ok(turmaService.listarTodos());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestBody AlterarStatusDTO dados) {
        turmaService.alterarStatus(id, dados.ativo());

        return ResponseEntity.noContent().build();
    }

}
