package com.tccds.sice.modules.curso;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.modules.curso.dto.CriarCursoDTO;
import com.tccds.sice.modules.curso.dto.CursoResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {
    
    private final CursoService cursoService;
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cadastrarCurso")
    public ResponseEntity<CursoResponseDTO> cadastrar(
        @Valid @RequestBody CriarCursoDTO dto
    ){
          
        CursoResponseDTO curso = cursoService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(curso);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listarCursos")
    public ResponseEntity<List<CursoResponseDTO>> listar(){
        return ResponseEntity.ok(cursoService.listarTodos());
    }

}
