package com.tccds.sice.modules.usuario;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.aluno.AlunoService;
import com.tccds.sice.modules.aluno.matricula.MatriculaService;
import com.tccds.sice.modules.credencial.Credencial;
import com.tccds.sice.modules.credencial.CredencialService;
import com.tccds.sice.modules.turma.Turma;
import com.tccds.sice.modules.turma.TurmaService;
import com.tccds.sice.modules.usuario.dto.CriarUsuarioDTO;
import com.tccds.sice.modules.usuario.dto.UsuarioResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CredencialService credencialService;
    private final MatriculaService matriculaService;
    private final AlunoService alunoService;
    private final TurmaService turmaService;

    public Usuario obterUsuarioLogado(){
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        
        if(authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("Nenhum usuario encontrado");
        }

        String identificador = authentication.getName();

        return usuarioRepository
                .findByCredencial_Identificador(identificador)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @Transactional
    public UsuarioResponseDTO criar(CriarUsuarioDTO dto) {

        Credencial credencial = credencialService.criar(dto.identificador(), dto.senha());

        Usuario usuario = new Usuario(
                dto.nome(),
                dto.email(),
                dto.perfil(),
                credencial);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        Set<Long> turmasIds = new HashSet<>();

        if (dto.perfil() == PerfilUsuario.ALUNO) {

            if (dto.turmasIds() == null || dto.turmasIds().isEmpty()) {
                throw new RuntimeException("Aluno deve possuir pelo menos uma turma");
            }

            Aluno aluno = alunoService.criar(usuarioSalvo);

            for (Long turmaId : dto.turmasIds()) {

                Turma turma = turmaService.buscarTurmaId(turmaId);
                matriculaService.criar(aluno, turma);

                turmasIds.add(turma.getId());

            }

        }

        return new UsuarioResponseDTO(
                usuarioSalvo,
                turmasIds);

    }

}
