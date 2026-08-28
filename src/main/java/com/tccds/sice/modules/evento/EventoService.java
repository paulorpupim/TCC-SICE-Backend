package com.tccds.sice.modules.evento;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.aluno.AlunoRepository;
import com.tccds.sice.modules.aluno.matricula.Matricula;
import com.tccds.sice.modules.evento.dto.CriarEventoDTO;
import com.tccds.sice.modules.evento.dto.EventoResponseDTO;
import com.tccds.sice.modules.evento.evento_turma.EventoTurma;
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

        private Set<Long> obterTurmasIds(Evento evento) {

                return evento.getDestinacoesTurma()
                                .stream()
                                .map(eventoTurma -> eventoTurma.getTurma().getId())
                                .collect(Collectors.toSet());
        }

        @Transactional
        public EventoResponseDTO criar(CriarEventoDTO dto) {

                Usuario usuarioLogado = usuarioService.obterUsuarioLogado();

                Evento evento = new Evento(
                                dto.titulo(),
                                dto.descricao(),
                                dto.dataHoraInicio(),
                                dto.status(),
                                dto.perfisDestinados(),
                                dto.etapasDestinadas(),
                                dto.modalidadesDestinadas(),
                                usuarioLogado);

                if (dto.turmasDestinadasIds() != null) {

                        for (Long turmaId : dto.turmasDestinadasIds()) {

                                Turma turma = turmaRepository.findById(turmaId)
                                                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

                                EventoTurma eventoTurma = new EventoTurma(
                                                evento,
                                                turma);

                                evento.getDestinacoesTurma().add(eventoTurma);
                        }
                }

                Evento eventoSalvo = eventoRepository.save(evento);

                return new EventoResponseDTO(
                                eventoSalvo,
                                obterTurmasIds(eventoSalvo));
        }

        @Transactional(readOnly = true)
        public List<EventoResponseDTO> listarTodos() {

                return eventoRepository.findAll()
                                .stream()
                                .map(evento -> new EventoResponseDTO(
                                                evento,
                                                obterTurmasIds(evento)))
                                .toList();

        }

        @Transactional(readOnly = true)
        public List<EventoResponseDTO> listarEventosUsuarioLogado() {

                Usuario usuario = usuarioService.obterUsuarioLogado();

                return eventoRepository
                                .findByStatus(StatusEvento.ATIVO)
                                .stream()
                                .filter(evento -> podeVisualizar(evento, usuario))
                                .map(evento -> new EventoResponseDTO(
                                                evento,
                                                obterTurmasIds(evento)))
                                .toList();
        }

        private boolean podeVisualizar(Evento evento, Usuario usuario) {
                PerfilUsuario perfil = usuario.getPerfil();

                if (perfil == PerfilUsuario.ADMIN || perfil == PerfilUsuario.SECRETARIA) {
                        return true;
                }

                if (!evento.getPerfisDestinados().contains(perfil)) {
                        return false;
                }

                if (perfil == PerfilUsuario.ALUNO) {
                        return alunoAtendeDestinacao(evento, usuario);
                }

                return true;

        }

        private boolean alunoAtendeDestinacao(Evento evento, Usuario usuario) {

                Aluno aluno = alunoRepository.findByUsuario(usuario)
                                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

                return aluno.getMatriculas()
                                .stream()
                                .filter(Matricula::isAtivo)
                                .anyMatch(matricula -> matriculaAtendeEvento(matricula, evento));

        }

        private boolean matriculaAtendeEvento(Matricula matricula, Evento evento) {

                Turma turma = matricula.getTurma();

                boolean etapaAtende = evento.getEtapasDestinadas().isEmpty()
                                ||
                                evento.getEtapasDestinadas()
                                                .contains(turma.getEtapa());

                boolean modalidadeAtende = evento.getModalidadesDestinadas().isEmpty()
                                ||
                                evento.getModalidadesDestinadas()
                                                .contains(turma.getCurso().getModalidade());

                boolean turmaAtende = evento.getDestinacoesTurma().isEmpty()
                                ||
                                evento.getDestinacoesTurma()
                                                .stream()
                                                .anyMatch(destino -> destino.getTurma().getId()
                                                                .equals(turma.getId()));

                return etapaAtende
                                && modalidadeAtende
                                && turmaAtende;

        }

}
