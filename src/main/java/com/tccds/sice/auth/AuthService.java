package com.tccds.sice.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.tccds.sice.auth.dto.LoginRequestDTO;
import com.tccds.sice.auth.dto.LoginResponseDTO;
import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.usuario.UsuarioRepository;
import com.tccds.sice.security.TokenService;
import com.tccds.sice.security.UsuarioDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final TokenService tokenService;
        private final UsuarioRepository usuarioRepository;

        public LoginResult login(LoginRequestDTO request) {

                Authentication authenticationRequest = UsernamePasswordAuthenticationToken
                                .unauthenticated(
                                                request.identificador(),
                                                request.senha());

                Authentication authentication = authenticationManager.authenticate(
                                authenticationRequest);

                UsuarioDetails usuarioDetails = (UsuarioDetails) authentication.getPrincipal();

                Usuario usuario = usuarioDetails.getUsuario();

                String token = tokenService.gerarToken(usuario);

                return new LoginResult(
                                token,
                                usuario.getPerfil(),
                                usuario.getCredencial().isPrimeiroAcesso());
        }

        public LoginResponseDTO me(String identificador) {

                Usuario usuario = usuarioRepository
                                .findByCredencial_Identificador(identificador)
                                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                return new LoginResponseDTO(
                                usuario.getPerfil(),
                                usuario.getCredencial().isPrimeiroAcesso());
        }

}