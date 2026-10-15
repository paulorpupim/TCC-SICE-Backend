package com.tccds.sice.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.credencial.Credencial;
import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PrimeiroAcessoAuthorizationManagerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PrimeiroAcessoAuthorizationManager manager;
    private RequestAuthorizationContext contexto;
    private JwtAuthenticationToken autenticacaoJwt;
    private Credencial credencial;
    private Usuario usuario;

    @BeforeEach
    void configurar() {
        manager = new PrimeiroAcessoAuthorizationManager(usuarioRepository);
        contexto = new RequestAuthorizationContext(new MockHttpServletRequest("GET", "/eventos/listarEventos"));

        Jwt jwt = Jwt.withTokenValue("token-de-teste")
                .header("alg", "HS256")
                .subject("matricula123")
                .claim("roles", List.of("ADMIN"))
                .build();
        autenticacaoJwt = new JwtAuthenticationToken(jwt);

        credencial = new Credencial("matricula123", "hash-de-teste");
        usuario = new Usuario("Usuário de teste", "teste@exemplo.com", PerfilUsuario.ADMIN, credencial);
    }

    @Test
    void bloqueiaQualquerPerfilComPrimeiroAcessoPendente() {
        when(usuarioRepository.findByCredencial_Identificador("matricula123"))
                .thenReturn(Optional.of(usuario));

        assertFalse(autorizado(autenticacaoJwt));
    }

    @Test
    void liberaComMesmoJwtAposTrocaDeSenha() {
        when(usuarioRepository.findByCredencial_Identificador("matricula123"))
                .thenReturn(Optional.of(usuario));

        assertFalse(autorizado(autenticacaoJwt));
        credencial.setPrimeiroAcesso(false);
        assertTrue(autorizado(autenticacaoJwt));
    }

    @Test
    void bloqueiaUsuarioInexistente() {
        when(usuarioRepository.findByCredencial_Identificador("matricula123"))
                .thenReturn(Optional.empty());

        assertFalse(autorizado(autenticacaoJwt));
    }

    @Test
    void bloqueiaContaDesativadaMesmoComJwtAntigo() {
        credencial.setPrimeiroAcesso(false);
        credencial.setAtivo(false);
        when(usuarioRepository.findByCredencial_Identificador("matricula123"))
                .thenReturn(Optional.of(usuario));

        assertFalse(autorizado(autenticacaoJwt));
    }

    @Test
    void bloqueiaAutenticacaoNaoJwt() {
        Authentication anonimo = new AnonymousAuthenticationToken(
                "chave-de-teste", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        assertFalse(autorizado(anonimo));
    }

    private boolean autorizado(Authentication autenticacao) {
        return manager.authorize(() -> autenticacao, contexto).isGranted();
    }
}
