package com.tccds.sice.modules.evento;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.modules.evento.dto.CriarEventoDTO;
import com.tccds.sice.modules.evento.dto.EventoResponseDTO;
import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.usuario.UsuarioRepository;
import com.tccds.sice.modules.z_enums.StatusEvento;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventoService {
    
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EventoResponseDTO criar(CriarEventoDTO dto){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identificador = authentication.getName();

        Usuario usuarioLogado = usuarioRepository.findByCredencial_Identificador(identificador)
            .orElseThrow(() -> new RuntimeException("Usuario não encontrado"));

        Evento evento = new Evento();

        evento.setTitulo(dto.titulo());
        evento.setDescricao(dto.descricao());
        evento.setDataHoraInicio(dto.dataHoraInicio());
        evento.setPerfisDestinados(new HashSet<>(dto.perfisDestinados()));
        evento.setSeriesDestinadas(new HashSet<>(dto.seriesDestinadas()));
        evento.setStatus(StatusEvento.ATIVO);
        evento.setCriadoEm(LocalDateTime.now());
        evento.setCriadoPor(usuarioLogado);

        Evento eventoSalvo = eventoRepository.save(evento);

        return new EventoResponseDTO(
            eventoSalvo.getId(),
            eventoSalvo.getTitulo(),
            eventoSalvo.getDescricao(),
            eventoSalvo.getDataHoraInicio(),
            eventoSalvo.getSeriesDestinadas(),
            eventoSalvo.getPerfisDestinados()
        );

    }

}
