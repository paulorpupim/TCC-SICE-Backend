package com.tccds.sice.modules.evento;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.evento.dto.CriarEventoDTO;
import com.tccds.sice.modules.evento.dto.EventoResponseDTO;
import com.tccds.sice.modules.evento.evento_turma.EventoTurma;
import com.tccds.sice.modules.turma.Turma;
import com.tccds.sice.modules.turma.TurmaRepository;
import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.usuario.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;

    private Set<Long> obterTurmasIds(Evento evento) {

        Set<Long> turmasIds = new HashSet<>();

        for (EventoTurma destino : evento.getDestinacoesTurma()) {
            turmasIds.add(destino.getTurma().getId());
        }

        return turmasIds;
    }

    @Transactional
    public EventoResponseDTO criar(CriarEventoDTO dto) {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String identificador = authentication.getName();

        Usuario usuarioLogado = usuarioRepository
                .findByCredencial_Identificador(identificador)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

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

}
