package com.tccds.sice.modules.cancelamento_aviso;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaComunicacaoCancelamentoRepository
        extends JpaRepository<TentativaComunicacaoCancelamento, Long> {
    List<TentativaComunicacaoCancelamento> findByComunicacaoIdOrderByNumeroTentativaAsc(Long comunicacaoId);
}
