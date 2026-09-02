package com.tccds.sice.modules.usuario;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.aluno.AlunoRepository;
import com.tccds.sice.modules.aluno.AlunoService;
import com.tccds.sice.modules.aluno.matricula.Matricula;
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
    private final MatriculaService matriculaService;
    private final AlunoRepository alunoRepository;

    private final CredencialService credencialService;
    private final AlunoService alunoService;
    private final TurmaService turmaService;

    public Usuario obterUsuarioLogado() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Nenhum usuario encontrado");
        }

        String identificador = authentication.getName();

        return usuarioRepository
                .findByCredencial_Identificador(identificador)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Set<Long> obterTurmasIds(Usuario usuario) {
        if (usuario.getPerfil() != PerfilUsuario.ALUNO) {
            return Set.of();
        }

        Aluno aluno = alunoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        
        return aluno.getMatriculas()
            .stream()
            .filter(Matricula::isAtivo)
            .map(matricula -> matricula.getTurma().getId())
            .collect(Collectors.toSet());
        
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

    public List<UsuarioResponseDTO> listarUsuariosPerfil(PerfilUsuario perfil) {

        return usuarioRepository.findByPerfil(perfil)
                .stream()
                .map(usuario -> {
                    Set<Long> turmasIds = obterTurmasIds(usuario);

                    return new UsuarioResponseDTO(usuario, turmasIds);
                })
                .toList();

    }

}
