package com.tccds.sice.modules.envio_aviso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;

public interface EnvioAvisoRepository extends JpaRepository<EnvioAviso, Long> {

        boolean existsByEventoIdAndGrupoWhatsappId(Long eventoId, Long grupoWhatsappId);

        List<EnvioAviso> findByEventoIdAndStatus(Long eventoId, StatusEnvioAviso status);

        List<EnvioAviso> findByEventoId(Long eventoId);

        @Query("""
                        SELECT e.id
                        FROM EnvioAviso e
                        WHERE e.status = :status
                          AND e.tentativas < 3
                          AND e.evento.status = :statusEvento
                          AND e.evento.dataInicio >= :hoje
                          AND (
                                e.ultimaTentativaEm IS NULL
                                OR e.ultimaTentativaEm <= :limiteTentativa
                          )
                        """)
        List<Long> buscarIdsPendentesParaRetry(
                        @Param("status") StatusEnvioAviso status,
                        @Param("statusEvento") StatusEvento statusEvento,
                        @Param("hoje") LocalDate hoje,
                        @Param("limiteTentativa") LocalDateTime limiteTentativa);

}