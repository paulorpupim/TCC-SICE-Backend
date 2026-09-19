package com.tccds.sice.modules.evento;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.aluno.AlunoRepository;
import com.tccds.sice.modules.aluno.matricula.Matricula;
import com.tccds.sice.modules.evento.dto.CriarEventoDTO;
import com.tccds.sice.modules.evento.dto.EventoResponseDTO;
import com.tccds.sice.modules.evento.evento_destino.EventoDestino;
import com.tccds.sice.modules.evento.evento_destino_turma.dto.CriarEventoDestinoDTO;
import com.tccds.sice.modules.turma.Turma;
import com.tccds.sice.modules.turma.TurmaRepository;
import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.usuario.UsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoService {

        private final EventoRepository eventoRepository;
        private final TurmaRepository turmaRepository;
        private final AlunoRepository alunoRepository;

        private final UsuarioService usuarioService;

        @Transactional
        public EventoResponseDTO criar(CriarEventoDTO dto) {

                validarDestinos(dto);

                Usuario usuarioLogado = usuarioService.obterUsuarioLogado();

                Evento evento = new Evento(
                                dto.titulo(),
                                dto.descricao(),
                                dto.dataInicio(),
                                dto.horaInicio(),
                                usuarioLogado);

                for (CriarEventoDestinoDTO destinoDTO : dto.destinos()) {

                        EventoDestino destino = new EventoDestino(
                                        destinoDTO.perfil(),
                                        destinoDTO.etapas(),
                                        destinoDTO.modalidades());

                        List<Turma> turmas = turmaRepository.findAllById(destinoDTO.turmasIds());

                        if (turmas.size() != destinoDTO.turmasIds().size()) {
                                throw new EntidadeNaoEncontradaException(
                                                "Uma ou mais turmas não foram encontradas!");
                        }

                        for (Turma turma : turmas) {
                                destino.adicionarTurma(turma);
                        }

                        evento.adicionarDestino(destino);
                }

                Evento eventoSalvo = eventoRepository.save(evento);

                return new EventoResponseDTO(
                                eventoSalvo);
        }

        private void validarDestinos(CriarEventoDTO dto) {

                Set<PerfilUsuario> perfis = new HashSet<>();

                for (CriarEventoDestinoDTO destino : dto.destinos()) {

                        if (destino.perfil() != PerfilUsuario.ALUNO
                                        && destino.perfil() != PerfilUsuario.PROFESSOR) {

                                throw new RuntimeException(
                                                "O evento só pode ser destinado a alunos ou professores.");
                        }

                        if (!perfis.add(destino.perfil())) {
                                throw new RuntimeException(
                                                "Não é permitido mais de um destino para o mesmo perfil.");
                        }

                        if (destino.perfil() == PerfilUsuario.PROFESSOR) {

                                boolean possuiFiltros = !destino.etapas().isEmpty()
                                                || !destino.modalidades().isEmpty()
                                                || !destino.turmasIds().isEmpty();

                                if (possuiFiltros) {
                                        throw new RuntimeException(
                                                        "Destinos para professores não podem possuir filtros.");
                                }
                        }
                }
        }

        @Transactional(readOnly = true)
        public List<EventoResponseDTO> listarTodos() {

                return eventoRepository.findAll()
                                .stream()
                                .map(EventoResponseDTO::new)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<EventoResponseDTO> listarEventosPorPerfil() {

                Usuario usuario = usuarioService.obterUsuarioLogado();

                List<Evento> eventos = eventoRepository.findByStatus(StatusEvento.ATIVO);

                PerfilUsuario perfil = usuario.getPerfil();

                if (perfil == PerfilUsuario.ADMIN || perfil == PerfilUsuario.SECRETARIA) {
                        return eventos.stream()
                                        .map(EventoResponseDTO::new)
                                        .toList();
                }

                if (perfil == PerfilUsuario.PROFESSOR) {
                        return eventos.stream()
                                        .filter(this::destinadoAProfessor)
                                        .map(EventoResponseDTO::new)
                                        .toList();
                }

                if (perfil == PerfilUsuario.ALUNO) {

                        Aluno aluno = alunoRepository.findByUsuario(usuario)
                                        .orElseThrow(() -> new EntidadeNaoEncontradaException(
                                                        "Aluno não encontrado"));

                        return eventos.stream()
                                        .filter(evento -> alunoPodeVisualizar(evento, aluno))
                                        .map(EventoResponseDTO::new)
                                        .toList();
                }

                return List.of();
        }

        private boolean destinadoAProfessor(Evento evento) {

                return evento.getDestinos()
                                .stream()
                                .anyMatch(destino -> destino.getPerfil() == PerfilUsuario.PROFESSOR);
        }

        private boolean alunoPodeVisualizar(Evento evento, Aluno aluno) {

                return evento.getDestinos()
                                .stream()
                                .filter(destino -> destino.getPerfil() == PerfilUsuario.ALUNO)
                                .anyMatch(destino -> alunoAtendeDestino(aluno, destino));
        }

        private boolean alunoAtendeDestino(
                        Aluno aluno,
                        EventoDestino destino) {

                return aluno.getMatriculas()
                                .stream()
                                .filter(Matricula::isAtivo)
                                .anyMatch(matricula -> matriculaAtendeDestino(matricula, destino));
        }

        private boolean matriculaAtendeDestino(
                        Matricula matricula,
                        EventoDestino destino) {

                Turma turma = matricula.getTurma();

                boolean etapaAtende = destino.getEtapas().isEmpty()
                                || destino.getEtapas().contains(turma.getEtapa());

                boolean modalidadeAtende = destino.getModalidades().isEmpty()
                                || destino.getModalidades().contains(turma.getModalidade());

                boolean turmaAtende = destino.getDestinacoesTurma().isEmpty()
                                || destino.getDestinacoesTurma()
                                                .stream()
                                                .anyMatch(destinacao -> destinacao.getTurma()
                                                                .getId()
                                                                .equals(turma.getId()));

                return etapaAtende
                                && modalidadeAtende
                                && turmaAtende;
        }
}