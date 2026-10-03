package com.tccds.sice.modules.usuario;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.exception.EntidadeNaoEncontradaException;
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
import com.tccds.sice.modules.usuario.dto.EditarUsuarioDTO;
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

        public Usuario buscarUsuarioId(Long id) {
                return usuarioRepository.findById(id).orElseThrow(
                                () -> new EntidadeNaoEncontradaException("Usuario não encontrado"));
        }

        @Transactional(readOnly = true)
        public UsuarioResponseDTO buscarUsuarioPorId(Long id) {

                Usuario usuario = buscarUsuarioId(id);

                Aluno aluno = null;

                if (usuario.getPerfil() == PerfilUsuario.ALUNO) {

                        aluno = alunoRepository.findByUsuario(usuario)
                                        .orElseThrow(() -> new EntidadeNaoEncontradaException(
                                                        "Aluno não encontrado"));
                }

                return new UsuarioResponseDTO(
                                usuario,
                                aluno);
        }

        public Usuario obterUsuarioLogado() {
                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new EntidadeNaoEncontradaException("Nenhum usuario encontrado");
                }

                String identificador = authentication.getName();

                return usuarioRepository
                                .findByCredencial_Identificador(identificador)
                                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado"));
        }

        @Transactional
        public UsuarioResponseDTO criar(CriarUsuarioDTO dto) {

                String senha = "123456";

                Credencial credencial = credencialService.criar(
                                dto.identificador(),
                                senha);

                Usuario usuario = new Usuario(
                                dto.nome(),
                                dto.email(),
                                dto.perfil(),
                                credencial);

                Usuario usuarioSalvo = usuarioRepository.save(usuario);

                Aluno aluno = null;

                if (dto.perfil() == PerfilUsuario.ALUNO) {

                        if (dto.turmasIds() == null ||
                                        dto.turmasIds().isEmpty()) {
                                throw new RuntimeException(
                                                "Aluno deve possuir pelo menos uma turma");
                        }

                        aluno = alunoService.criar(usuarioSalvo);

                        for (Long turmaId : dto.turmasIds()) {

                                Turma turma = turmaService.buscarTurmaId(turmaId);

                                matriculaService.criar(
                                                aluno,
                                                turma);
                        }
                }

                return new UsuarioResponseDTO(
                                usuarioSalvo,
                                aluno);
        }

        @Transactional
        public UsuarioResponseDTO editar(
                        Long id,
                        EditarUsuarioDTO dto) {

                Usuario usuario = buscarUsuarioId(id);

                validarEdicao(usuario, dto);

                usuario.setNome(dto.nome());
                usuario.setEmail(dto.email());

                usuario.getCredencial()
                                .setIdentificador(dto.identificador());

                Aluno aluno = null;

                if (usuario.getPerfil() == PerfilUsuario.ALUNO) {

                        aluno = alunoRepository.findByUsuario(usuario)
                                        .orElseThrow(() -> new EntidadeNaoEncontradaException(
                                                        "Aluno não encontrado"));

                        atualizarTurmasAluno(
                                        aluno,
                                        dto.turmasIds());
                }

                Usuario usuarioSalvo = usuarioRepository.save(usuario);

                return new UsuarioResponseDTO(
                                usuarioSalvo,
                                aluno);
        }

        private void validarEdicao(
                        Usuario usuario,
                        EditarUsuarioDTO dto) {

                if (usuario.getPerfil() == PerfilUsuario.ALUNO
                                && dto.turmasIds().isEmpty()) {
                        throw new RuntimeException(
                                        "Aluno deve possuir pelo menos uma turma.");
                }

                if (usuario.getPerfil() != PerfilUsuario.ALUNO
                                && !dto.turmasIds().isEmpty()) {
                        throw new RuntimeException(
                                        "Apenas alunos podem possuir turmas.");
                }
        }

        private void atualizarTurmasAluno(
                        Aluno aluno,
                        Set<Long> novasTurmasIds) {

                aluno.getMatriculas()
                                .stream()
                                .filter(Matricula::isAtivo)
                                .filter(matricula -> !novasTurmasIds.contains(
                                                matricula.getTurma().getId()))
                                .forEach(matricula -> matricula.setAtivo(false));

                for (Long turmaId : novasTurmasIds) {

                        Matricula matriculaExistente = aluno.getMatriculas()
                                        .stream()
                                        .filter(matricula -> matricula.getTurma()
                                                        .getId()
                                                        .equals(turmaId))
                                        .findFirst()
                                        .orElse(null);

                        if (matriculaExistente != null) {

                                matriculaExistente.setAtivo(true);

                        } else {

                                Turma turma = turmaService.buscarTurmaId(turmaId);

                                matriculaService.criar(
                                                aluno,
                                                turma);
                        }
                }
        }

        @Transactional(readOnly = true)
        public List<UsuarioResponseDTO> listarUsuariosPerfil(
                        PerfilUsuario perfil) {

                List<Usuario> usuarios = usuarioRepository.findByPerfil(perfil);

                if (perfil == PerfilUsuario.ALUNO) {

                        return usuarios.stream()
                                        .map(usuario -> {

                                                Aluno aluno = alunoRepository.findByUsuario(usuario)
                                                                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                                                                                "Aluno não encontrado"));

                                                return new UsuarioResponseDTO(
                                                                usuario,
                                                                aluno);
                                        })
                                        .toList();
                }

                return usuarios.stream()
                                .map(usuario -> new UsuarioResponseDTO(
                                                usuario,
                                                null))
                                .toList();
        }

        @Transactional
        public void alterarStatus(Long id, boolean ativo) {
                Usuario usuario = buscarUsuarioId(id);

                usuario.getCredencial().setAtivo(ativo);
        }

}
