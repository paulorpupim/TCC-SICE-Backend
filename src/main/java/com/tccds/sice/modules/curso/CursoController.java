package com.tccds.sice.modules.curso;

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

import com.tccds.sice.modules.curso.dto.CriarCursoDTO;
import com.tccds.sice.modules.curso.dto.CursoResponseDTO;
import com.tccds.sice.modules.curso.dto.EditarCursoDTO;
import com.tccds.sice.modules.dto.AlterarStatusDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cursoService.buscarCursoPorId(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastrarCurso")
    public ResponseEntity<CursoResponseDTO> cadastrar(
            @Valid @RequestBody CriarCursoDTO dto) {

        CursoResponseDTO curso = cursoService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(curso);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listarCursos")
    public ResponseEntity<List<CursoResponseDTO>> listar() {
        return ResponseEntity.ok(cursoService.listarTodos());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> editar(
            @PathVariable Long id, @RequestBody EditarCursoDTO dto) {
        return ResponseEntity.ok(cursoService.editar(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestBody AlterarStatusDTO dados) {
        cursoService.alterarStatus(id, dados.ativo());

        return ResponseEntity.noContent().build();
    }

}
