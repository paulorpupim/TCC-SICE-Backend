package com.tccds.sice.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService
        implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(
            String identificador
    ) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByCredencial_Identificador(
                        identificador
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado"
                        )
                );

        return new UsuarioDetails(usuario);
    }
}