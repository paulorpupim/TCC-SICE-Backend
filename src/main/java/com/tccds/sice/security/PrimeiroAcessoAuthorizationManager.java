package com.tccds.sice.security;

import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import com.tccds.sice.modules.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Impede operações de negócio enquanto a primeira troca de senha não ocorrer.
 * As exceções (/auth/me, /auth/logout e /usuarios/alterarSenha) são declaradas
 * explicitamente na SecurityConfig, antes de anyRequest().
 */
@Component
@RequiredArgsConstructor
public class PrimeiroAcessoAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final UsuarioRepository usuarioRepository;

    @Override
    public AuthorizationResult authorize(
            Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context) {

        Authentication autenticacao = authentication.get();

        // O backend utiliza JWT via cookie. Não confiamos em logins anônimos
        // nem em dados controlados pelo cliente para liberar o primeiro acesso.
        if (!(autenticacao instanceof JwtAuthenticationToken jwtAuthentication)
                || !autenticacao.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        // Consultar a credencial atual evita manter permissões antigas em JWTs
        // emitidos antes da troca de senha ou da desativação da conta.
        boolean autorizado = usuarioRepository
                .findByCredencial_Identificador(jwtAuthentication.getName())
                .map(usuario -> usuario.getCredencial().isAtivo()
                        && !usuario.getCredencial().isPrimeiroAcesso())
                .orElse(false);

        return new AuthorizationDecision(autorizado);
    }
}
