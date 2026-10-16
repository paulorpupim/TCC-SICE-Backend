package com.tccds.sice.modules.evento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import com.tccds.sice.enums.StatusEvento;

public interface EventoRepository extends JpaRepository<Evento, Long> {

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT e FROM Evento e WHERE e.id = :id")
        Optional<Evento> buscarParaCancelamento(@Param("id") Long id);


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
