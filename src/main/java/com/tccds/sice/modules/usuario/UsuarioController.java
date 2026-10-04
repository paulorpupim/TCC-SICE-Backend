package com.tccds.sice.modules.usuario;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.dto.AlterarStatusDTO;
import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.usuario.dto.CriarUsuarioDTO;
import com.tccds.sice.modules.usuario.dto.EditarUsuarioDTO;
import com.tccds.sice.modules.usuario.dto.UsuarioResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorId(id));
    }

    @PostMapping("/cadastrarUsuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(
            @Valid @RequestBody CriarUsuarioDTO dto) {

        UsuarioResponseDTO usuario = usuarioService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);

    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> editar(
            @PathVariable Long id, @RequestBody EditarUsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.editar(id, dto));
    }

    @GetMapping("/listarUsuariosPerfil")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorPerfil(
            @RequestParam PerfilUsuario perfil) {
        return ResponseEntity.ok(usuarioService.listarUsuariosPerfil(perfil));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestBody AlterarStatusDTO dados) {
        usuarioService.alterarStatus(id, dados.ativo());

        return ResponseEntity.noContent().build();
    }

}
