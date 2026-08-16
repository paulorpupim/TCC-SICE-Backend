package com.tccds.sice.modules.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.aluno.AlunoRepository;
import com.tccds.sice.modules.credencial.Credencial;
import com.tccds.sice.modules.usuario.dto.CriarUsuarioDTO;
import com.tccds.sice.modules.usuario.dto.UsuarioResponseDTO;
import com.tccds.sice.modules.z_enums.PerfilUsuario;
import com.tccds.sice.modules.z_enums.Serie;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO criar(CriarUsuarioDTO dto){

        Credencial credencial = new Credencial();

        credencial.setIdentificador(dto.identificador());
        credencial.setSenhaHash(passwordEncoder.encode(dto.senha()));
        credencial.setAtivo(true);
        credencial.setPrimeiroAcesso(true);

        Usuario usuario = new Usuario();

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setPerfil(dto.perfil());
        usuario.setCredencial(credencial);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        Serie serie = null;

        if(dto.perfil() == PerfilUsuario.ALUNO){

            Aluno aluno = new Aluno();

            aluno.setSerie(dto.serie());
            aluno.setUsuario(usuario);

            alunoRepository.save(aluno);

            serie = aluno.getSerie();

        }

        return new UsuarioResponseDTO(
            usuarioSalvo.getId(),
            usuarioSalvo.getNome(),
            usuarioSalvo.getEmail(),
            usuarioSalvo.getPerfil(),
            serie,
            usuarioSalvo.getCredencial().getIdentificador()
        );

    }

}
