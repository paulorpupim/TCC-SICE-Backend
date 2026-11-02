package com.tccds.sice.modules.comunicacao_cancelamento.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.modules.comunicacao_cancelamento._tentativa_comunicacao_cancelamento.TentativaComunicacaoCancelamentoRepository;
import com.tccds.sice.modules.comunicacao_cancelamento.dto.ComunicacaoCancelamentoResponseDTO;
import com.tccds.sice.modules.comunicacao_cancelamento.repository.ComunicacaoCancelamentoRepository;
import com.tccds.sice.modules.evento.EventoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultaComunicacaoCancelamentoService {
    private final EventoRepository eventoRepository;
    private final ComunicacaoCancelamentoRepository repository;
    private final TentativaComunicacaoCancelamentoRepository tentativaRepository;

    @Transactional(readOnly = true)
    public List<ComunicacaoCancelamentoResponseDTO> listar(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new EntidadeNaoEncontradaException("Evento nao encontrado.");
        }
        return repository.findByEventoIdOrderByIdAsc(eventoId).stream()
            .map(c -> new ComunicacaoCancelamentoResponseDTO(c,
                tentativaRepository.findByComunicacaoIdOrderByNumeroTentativaAsc(c.getId())))
            .toList();
    }
}
