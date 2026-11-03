package com.tccds.sice.modules.comunicacao_cancelamento.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tccds.sice.enums.StatusComunicacaoCancelamento;
import com.tccds.sice.modules.comunicacao_cancelamento.entity.ComunicacaoCancelamento;

public interface ComunicacaoCancelamentoRepository extends JpaRepository<ComunicacaoCancelamento, Long>, JpaSpecificationExecutor<ComunicacaoCancelamento> {

    /** Carrega evento e grupo junto com os registros paginados. */
    @Override
    @EntityGraph(attributePaths = {"evento", "grupoWhatsapp"})
    Page<ComunicacaoCancelamento> findAll(Specification<ComunicacaoCancelamento> spec, Pageable pageable);

    
    /** Busca somente o comunicado solicitado, com evento e grupo para montar o DTO. */
    @EntityGraph(attributePaths = {"evento", "grupoWhatsapp"})
    @Query("select c from ComunicacaoCancelamento c where c.id = :id")
    Optional<ComunicacaoCancelamento> buscarDetalhesPorId(@Param("id") Long id);

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
