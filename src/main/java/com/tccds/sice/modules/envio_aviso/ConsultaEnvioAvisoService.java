package com.tccds.sice.modules.envio_aviso;

import java.util.Locale;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.modules.envio_aviso.dto.EnvioAvisoResponseDTO;
import com.tccds.sice.modules.envio_aviso.dto.PaginaEnviosResponseDTO;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.TentativaEnvioAvisoRepository;
import com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso.dto.TentativaEnvioAvisoResponseDTO;

import lombok.RequiredArgsConstructor;

/** Camada exclusiva de leitura: nao modifica nem dispara envios. */
@Service
@RequiredArgsConstructor
public class ConsultaEnvioAvisoService {

    private final EnvioAvisoRepository repository;
    private final TentativaEnvioAvisoRepository tentativaRepository;

    @Transactional(readOnly = true)
    public PaginaEnviosResponseDTO listar(StatusEnvioAviso status, String busca,
                                           int pagina, int tamanho) {
        String filtro = busca == null ? "" : busca.trim();
        boolean temBusca = !filtro.isEmpty();
        PageRequest paginacao = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "id"));

        Page<EnvioAviso> resultado;
        if (temBusca) {
            String padrao = "%" + filtro.toLowerCase(Locale.ROOT) + "%";
            resultado = status == null
                    ? repository.consultarPorBusca(padrao, paginacao)
                    : repository.consultarPorStatusEBusca(status, padrao, paginacao);
        } else {
            resultado = status == null
                    ? repository.consultarTodos(paginacao)
                    : repository.consultarPorStatus(status, paginacao);
        }

        return new PaginaEnviosResponseDTO(resultado);
    }

    @Transactional(readOnly = true)
    public EnvioAvisoResponseDTO buscarPorId(Long id) {
        return repository.consultarPorId(id)
                .map(EnvioAvisoResponseDTO::new)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Envio de aviso não encontrado."));
    }
    @Transactional(readOnly = true)
    public List<TentativaEnvioAvisoResponseDTO> listarTentativas(Long envioId) {
        if (!repository.existsById(envioId)) {
            throw new EntidadeNaoEncontradaException("Envio de aviso não encontrado.");
        }
        return tentativaRepository.findByEnvioAvisoIdOrderByNumeroTentativaAsc(envioId)
                .stream().map(TentativaEnvioAvisoResponseDTO::new).toList();
    }
}
