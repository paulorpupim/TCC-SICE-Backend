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

    @PostMapping("/cadastrarEvento")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<EventoResponseDTO> cadastrar(
            @Valid @RequestBody CriarEventoDTO dto) {

        EventoResponseDTO evento = eventoService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(evento);

    }

    @GetMapping("/listarEventos")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
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
        System.out.println(id);
        return ResponseEntity.ok(eventoService.buscarEventoPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventoResponseDTO> editar(
            @PathVariable Long id,
            @RequestBody @Valid EditarEventoDTO dto) {

        EventoResponseDTO evento = eventoService.editar(id, dto);

        return ResponseEntity.ok(evento);
    }

    @PatchMapping("/{id}/cancelar")
    public EventoResponseDTO cancelar(
            @PathVariable Long id) {
        return eventoService.cancelar(id);
    }

    @PatchMapping("/{id}/reativar")
    public EventoResponseDTO reativar(
            @PathVariable Long id) {
        return eventoService.reativar(id);
    }

}
