package com.tccds.sice.modules.cancelamento_aviso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComunicacaoCancelamentoRepository extends JpaRepository<ComunicacaoCancelamento, Long> {
    boolean existsByEventoId(Long eventoId);

    List<ComunicacaoCancelamento> findByEventoIdOrderByIdAsc(Long eventoId);

    @Query("select c.id from ComunicacaoCancelamento c where c.status = :status and c.tentativas = 0 and c.processandoDesde is null")
    List<Long> buscarPendentesNaoIniciadas(@Param("status") StatusComunicacaoCancelamento status);

    @Query("select c.id from ComunicacaoCancelamento c where c.evento.id = :eventoId and c.status = :status")
    List<Long> buscarIds(@Param("eventoId") Long eventoId,
                         @Param("status") StatusComunicacaoCancelamento status);

    @Query("select c.id from ComunicacaoCancelamento c where c.processandoDesde is not null and c.processandoDesde <= :limite")
    List<Long> buscarReservasExpiradas(@Param("limite") LocalDateTime limite);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ComunicacaoCancelamento c where c.id = :id")
    Optional<ComunicacaoCancelamento> buscarParaReserva(@Param("id") Long id);
}
