package com.tccds.sice.modules.usuario;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.modules.usuario.dto.criarUsuarioDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastrar")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario criar(@RequestBody criarUsuarioDTO dto){
        return usuarioService.criar(dto);
    }
    
}
