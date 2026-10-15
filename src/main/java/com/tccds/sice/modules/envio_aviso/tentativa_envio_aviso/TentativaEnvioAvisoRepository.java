package com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaEnvioAvisoRepository extends JpaRepository<TentativaEnvioAviso, Long> {
    List<TentativaEnvioAviso> findByEnvioAvisoIdOrderByNumeroTentativaAsc(Long envioAvisoId);
}
