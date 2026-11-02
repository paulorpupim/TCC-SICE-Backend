package com.tccds.sice.modules.comunicacao_cancelamento._tentativa_comunicacao_cancelamento;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaComunicacaoCancelamentoRepository extends JpaRepository<TentativaComunicacaoCancelamento, Long> {
    
    List<TentativaComunicacaoCancelamento> findByComunicacaoIdOrderByNumeroTentativaAsc(Long comunicacaoId);

}
