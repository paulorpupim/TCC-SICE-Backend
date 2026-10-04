package com.tccds.sice.modules.grupo_whatsapp;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tccds.sice.enums.TipoGrupo;

@Repository
public interface GrupoWhatsappRepository
        extends JpaRepository<GrupoWhatsapp, Long> {

    boolean existsByIdentificador(String identificador);

    boolean existsByIdentificadorAndIdNot(String identificador, Long id);

    boolean existsByTurmaId(Long turmaId);

    boolean existsByTurmaIdAndIdNot(Long turmaId, Long id);

    boolean existsByTipo(TipoGrupo tipo);

    boolean existsByTipoAndIdNot(TipoGrupo tipo, Long id);

    Optional<GrupoWhatsapp> findByTipoAndAtivoTrue(TipoGrupo tipo);

    Optional<GrupoWhatsapp> findByTurmaIdAndAtivoTrue(Long turmaId);
}