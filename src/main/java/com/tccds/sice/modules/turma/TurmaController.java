package com.tccds.sice.modules.turma;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.modules.turma.dto.CriarTurmaDTO;
import com.tccds.sice.modules.turma.dto.TurmaResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/turmas")
@RequiredArgsConstructor
public class TurmaController {
    
    private final TurmaService turmaService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastrarTurma")
    public ResponseEntity<TurmaResponseDTO> cadastrar(
        @Valid @RequestBody CriarTurmaDTO dto
    ){

        TurmaResponseDTO turma = turmaService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(turma);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listarTurmas")
    public ResponseEntity<List<TurmaResponseDTO>> listar(){
        return ResponseEntity.ok(turmaService.listarTodos());
    }

}
