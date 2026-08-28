package com.tccds.sice.modules.evento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tccds.sice.enums.StatusEvento;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    
    List<Evento> findByStatus(StatusEvento status);

}
