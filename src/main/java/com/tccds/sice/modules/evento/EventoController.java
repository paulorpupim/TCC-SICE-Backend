package com.tccds.sice.modules.evento;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.modules.evento.dto.CriarEventoDTO;
import com.tccds.sice.modules.evento.dto.EditarEventoDTO;
import com.tccds.sice.modules.evento.dto.EventoResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    @PostMapping("/cadastrarEvento")
    public ResponseEntity<EventoResponseDTO> cadastrar(
            @Valid @RequestBody CriarEventoDTO dto) {

        EventoResponseDTO evento = eventoService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(evento);

    }

    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    @GetMapping("/listarEventos")
    public ResponseEntity<List<EventoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/listarEventosPerfil")
    public ResponseEntity<List<EventoResponseDTO>> listarEventosUsuarioLogado() {
        return ResponseEntity.ok(eventoService.listarEventosPorPerfil());
    }

    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarEventoPorId(id));
    }

    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> editar(
            @PathVariable Long id,
            @RequestBody @Valid EditarEventoDTO dto) {

        EventoResponseDTO evento = eventoService.editar(id, dto);

        return ResponseEntity.ok(evento);
    }

    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    @PatchMapping("/{id}/cancelar")
    public EventoResponseDTO cancelar(
            @PathVariable Long id) {
        return eventoService.cancelar(id);
    }

    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    @PatchMapping("/{id}/desativar")
    public EventoResponseDTO desativar(
            @PathVariable Long id) {
        return eventoService.desativar(id);
    }

    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    @PatchMapping("/{id}/reativar")
    public EventoResponseDTO reativar(
            @PathVariable Long id) {
        return eventoService.reativar(id);
    }

}