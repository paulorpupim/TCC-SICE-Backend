package com.tccds.sice.modules.envio_aviso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;

public interface EnvioAvisoRepository extends JpaRepository<EnvioAviso, Long> {

        // Consultas separadas para nao passar parametros nulos a expressao LIKE/LOWER.
        // Todas carregam as associacoes ManyToOne para evitar N+1 na listagem.
        @Query(value = """
                        SELECT envio
                        FROM EnvioAviso envio
                        JOIN FETCH envio.evento
                        JOIN FETCH envio.grupoWhatsapp
                        """,
               countQuery = "SELECT COUNT(envio) FROM EnvioAviso envio")
        Page<EnvioAviso> consultarTodos(Pageable pageable);

        @Query(value = """
                        SELECT envio
                        FROM EnvioAviso envio
                        JOIN FETCH envio.evento
                        JOIN FETCH envio.grupoWhatsapp
                        WHERE envio.status = :status
                        """,
               countQuery = """
                        SELECT COUNT(envio)
                        FROM EnvioAviso envio
                        WHERE envio.status = :status
                        """)
        Page<EnvioAviso> consultarPorStatus(
                        @Param("status") StatusEnvioAviso status, Pageable pageable);

        // :padrao e sempre texto nao nulo, ja normalizado: "%" + busca.toLowerCase() + "%".
        @Query(value = """
                        SELECT envio
                        FROM EnvioAviso envio
                        JOIN FETCH envio.evento evento
                        JOIN FETCH envio.grupoWhatsapp grupo
                        WHERE LOWER(evento.titulo) LIKE :padrao
                           OR LOWER(grupo.nome) LIKE :padrao
                        """,
               countQuery = """
                        SELECT COUNT(envio)
                        FROM EnvioAviso envio
                        JOIN envio.evento evento
                        JOIN envio.grupoWhatsapp grupo
                        WHERE LOWER(evento.titulo) LIKE :padrao
                           OR LOWER(grupo.nome) LIKE :padrao
                        """)
        Page<EnvioAviso> consultarPorBusca(
                        @Param("padrao") String padrao, Pageable pageable);

        @Query(value = """
                        SELECT envio
                        FROM EnvioAviso envio
                        JOIN FETCH envio.evento evento
                        JOIN FETCH envio.grupoWhatsapp grupo
                        WHERE envio.status = :status
                          AND (LOWER(evento.titulo) LIKE :padrao
                               OR LOWER(grupo.nome) LIKE :padrao)
                        """,
               countQuery = """
                        SELECT COUNT(envio)
                        FROM EnvioAviso envio
                        JOIN envio.evento evento
                        JOIN envio.grupoWhatsapp grupo
                        WHERE envio.status = :status
                          AND (LOWER(evento.titulo) LIKE :padrao
                               OR LOWER(grupo.nome) LIKE :padrao)
                        """)
        Page<EnvioAviso> consultarPorStatusEBusca(
                        @Param("status") StatusEnvioAviso status,
                        @Param("padrao") String padrao,
                        Pageable pageable);

        @Query("""
                SELECT envio FROM EnvioAviso envio
                JOIN FETCH envio.evento
                JOIN FETCH envio.grupoWhatsapp
                WHERE envio.id = :id
                """)
        Optional<EnvioAviso> consultarPorId(@Param("id") Long id);


        /** A reserva e feita sob bloqueio de linha no PostgreSQL. */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT envio FROM EnvioAviso envio WHERE envio.id = :id")
        Optional<EnvioAviso> buscarParaReserva(@Param("id") Long id);

        @Query("SELECT envio.id FROM EnvioAviso envio WHERE envio.evento.id = :eventoId AND envio.status = :status")
        List<Long> buscarIdsDoEventoPorStatus(@Param("eventoId") Long eventoId,
                        @Param("status") StatusEnvioAviso status);

        @Query("SELECT envio.id FROM EnvioAviso envio WHERE envio.processandoDesde IS NOT NULL AND envio.processandoDesde <= :limite")
        List<Long> buscarReservasExpiradas(@Param("limite") LocalDateTime limite);

        boolean existsByEventoId(Long eventoId);

        boolean existsByEventoIdAndGrupoWhatsappId(Long eventoId, Long grupoWhatsappId);

        List<EnvioAviso> findByEventoIdAndStatus(Long eventoId, StatusEnvioAviso status);

        List<EnvioAviso> findByEventoId(Long eventoId);

        List<EnvioAviso> findByEventoIdOrderByIdAsc(Long eventoId);

        @Query("""
                        SELECT e.id
                        FROM EnvioAviso e
                        WHERE e.status = :status
                          AND e.tentativas < 3
                          AND e.processandoDesde IS NULL
                          AND e.evento.status = :statusEvento
                          AND e.evento.dataInicio >= :hoje
                          AND e.evento.dataAviso <= :hoje
                          AND e.grupoWhatsapp.ativo = true
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
