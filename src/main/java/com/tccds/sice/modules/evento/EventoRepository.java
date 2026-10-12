package com.tccds.sice.modules.evento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tccds.sice.enums.StatusEvento;

public interface EventoRepository extends JpaRepository<Evento, Long> {

        @EntityGraph(attributePaths = {
                        "destinos",
                        "destinos.destinacoesTurma",
                        "destinos.destinacoesTurma.turma"
        })
        List<Evento> findByStatusInOrderByDataInicioAscHoraInicioAsc(
                        List<StatusEvento> status);

        @EntityGraph(attributePaths = {
                        "destinos",
                        "destinos.destinacoesTurma",
                        "destinos.destinacoesTurma.turma"
        })
        List<Evento> findByStatusAndDataAvisoLessThanEqualAndDataInicioGreaterThanEqual(
                        StatusEvento status,
                        LocalDate dataAviso,
                        LocalDate dataInicio);

}
