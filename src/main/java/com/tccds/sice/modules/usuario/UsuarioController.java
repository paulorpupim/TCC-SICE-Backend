package com.tccds.sice.modules.usuario;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.usuario.dto.CriarUsuarioDTO;
import com.tccds.sice.modules.usuario.dto.UsuarioResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastrarUsuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(
        @Valid @RequestBody CriarUsuarioDTO dto
    ){

        UsuarioResponseDTO usuario = usuarioService.criar(dto);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(usuario);

    }

    @GetMapping("/listarUsuariosPerfil")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorPerfil(
        @RequestParam PerfilUsuario perfil
    ){
        return ResponseEntity.ok(usuarioService.listarUsuariosPerfil(perfil));
    }
    
}
