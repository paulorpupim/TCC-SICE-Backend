package com.tccds.sice.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.tccds.sice.auth.dto.LoginRequest;
import com.tccds.sice.auth.dto.LoginResponse;
import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.security.TokenService;
import com.tccds.sice.security.UsuarioDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager
            authenticationManager;

    private final TokenService tokenService;


    public LoginResponse login(
            LoginRequest request
    ) {

        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken
                        .unauthenticated(
                                request.identificador(),
                                request.senha()
                        );


        Authentication authentication =
                authenticationManager.authenticate(
                        authenticationRequest
                );


        UsuarioDetails usuarioDetails =
                (UsuarioDetails)
                        authentication
                                .getPrincipal();


        Usuario usuario =
                usuarioDetails
                        .getUsuario();


        String token =
                tokenService
                        .gerarToken(usuario);


        return new LoginResponse(
                token,
                "Bearer",
                usuario.getPerfil(),
                usuario
                        .getCredencial()
                        .isPrimeiroAcesso()
        );
    }
}