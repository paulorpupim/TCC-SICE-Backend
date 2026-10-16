package com.tccds.sice.modules.usuario;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.exception.RegraNegocioCadastroException;
import com.tccds.sice.modules.aluno.AlunoRepository;
import com.tccds.sice.modules.aluno.AlunoService;
import com.tccds.sice.modules.aluno.matricula.MatriculaService;
import com.tccds.sice.modules.credencial.Credencial;
import com.tccds.sice.modules.credencial.CredencialService;
import com.tccds.sice.modules.turma.TurmaService;
import com.tccds.sice.modules.usuario.dto.RedefinirSenhaUsuarioDTO;

@ExtendWith(MockitoExtension.class)
class RedefinirSenhaUsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock MatriculaService matriculaService;
    @Mock AlunoRepository alunoRepository;
    @Mock CredencialService credencialService;
    @Mock AlunoService alunoService;
    @Mock TurmaService turmaService;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UsuarioService service;

    private Usuario administrador;

    private Usuario usuario(Long id, PerfilUsuario perfil, boolean ativo) {
        Credencial c = new Credencial("login-" + id, "hash-antigo");
        c.setAtivo(ativo);
        c.setPrimeiroAcesso(false);
        Usuario u = new Usuario("Usuário " + id, "u@teste.com", perfil, c);
        u.setId(id);
        return u;
    }

    @BeforeEach
    void entrarComoAdmin() {
        administrador = usuario(1L, PerfilUsuario.ADMIN, true);
        var auth = new UsernamePasswordAuthenticationToken(
                "login-1", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private void simularAdminLogado() {
        when(usuarioRepository.findByCredencial_Identificador("login-1"))
                .thenReturn(Optional.of(administrador));
    }

    @Test
    void redefineSenhaEExigePrimeiroAcessoSemReativarUsuario() {
        Usuario alvo = usuario(10L, PerfilUsuario.ALUNO, false);
        simularAdminLogado();
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(alvo));
        when(passwordEncoder.encode("temporaria123")).thenReturn("novo-hash");

        service.redefinirSenha(10L, new RedefinirSenhaUsuarioDTO("temporaria123"));

        assertEquals("novo-hash", alvo.getCredencial().getSenhaHash());
        assertTrue(alvo.getCredencial().isPrimeiroAcesso());
        assertFalse(alvo.getCredencial().isAtivo());
        verify(passwordEncoder).matches("temporaria123", "hash-antigo");
        verify(passwordEncoder).encode("temporaria123");
    }

    @Test
    void naoPermiteReutilizarSenhaExistente() {
        Usuario alvo = usuario(11L, PerfilUsuario.PROFESSOR, true);
        simularAdminLogado();
        when(usuarioRepository.findById(11L)).thenReturn(Optional.of(alvo));
        when(passwordEncoder.matches("mesma-senha", "hash-antigo")).thenReturn(true);

        assertThrows(RegraNegocioCadastroException.class,
                () -> service.redefinirSenha(11L, new RedefinirSenhaUsuarioDTO("mesma-senha")));
        assertEquals("hash-antigo", alvo.getCredencial().getSenhaHash());
        assertFalse(alvo.getCredencial().isPrimeiroAcesso());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void naoPermiteAdminRedefinirSuaPropriaSenha() {
        simularAdminLogado();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(administrador));

        assertThrows(RegraNegocioCadastroException.class,
                () -> service.redefinirSenha(1L, new RedefinirSenhaUsuarioDTO("senha123")));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void usuarioInexistenteRetornaErroSemModificarSenha() {
        simularAdminLogado();
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> service.redefinirSenha(404L, new RedefinirSenhaUsuarioDTO("senha123")));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void redefinicaoNaoAlteraIdentificadorPerfilOuEmail() {
        Usuario alvo = usuario(12L, PerfilUsuario.SECRETARIA, true);
        simularAdminLogado();
        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(alvo));
        when(passwordEncoder.encode("senhaNova12")).thenReturn("hash-novo");

        service.redefinirSenha(12L, new RedefinirSenhaUsuarioDTO("senhaNova12"));

        assertEquals("login-12", alvo.getCredencial().getIdentificador());
        assertEquals(PerfilUsuario.SECRETARIA, alvo.getPerfil());
        assertEquals("u@teste.com", alvo.getEmail());
        assertTrue(alvo.getCredencial().isAtivo());
    }
}
