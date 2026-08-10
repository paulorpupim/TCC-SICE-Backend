package com.tccds.sice.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tccds.sice.modules.credencial.Credencial;
import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.usuario.UsuarioRepository;
import com.tccds.sice.modules.z_shared.enums.PerfilUsuario;

import lombok.RequiredArgsConstructor;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String identificador = "00000";

        boolean usuarioJaExiste =
                usuarioRepository
                        .findByCredencial_Identificador(identificador)
                        .isPresent();

        if (usuarioJaExiste) {
            return;
        }

        Credencial credencial = new Credencial();

        credencial.setIdentificador(identificador);

        credencial.setSenhaHash(
                passwordEncoder.encode("123456")
        );

        credencial.setPrimeiroAcesso(true);
        credencial.setAtivo(true);


        Usuario usuario = new Usuario();

        usuario.setNome("Administrador");
        usuario.setEmail("admin@sice.com");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setCredencial(credencial);


        usuarioRepository.save(usuario);

        System.out.println(
                "Usuário admin criado!"
        );
    }
}