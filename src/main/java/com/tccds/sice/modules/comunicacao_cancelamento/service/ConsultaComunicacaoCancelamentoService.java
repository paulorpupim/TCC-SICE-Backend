package com.tccds.sice.modules.comunicacao_cancelamento.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.StatusComunicacaoCancelamento;
import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.modules.comunicacao_cancelamento._tentativa_comunicacao_cancelamento.TentativaComunicacaoCancelamentoRepository;
import com.tccds.sice.modules.comunicacao_cancelamento.dto.ComunicacaoCancelamentoResponseDTO;
import com.tccds.sice.modules.comunicacao_cancelamento.dto.PaginaComunicacoesCancelamentoResponseDTO;
import com.tccds.sice.modules.comunicacao_cancelamento.dto.ResumoComunicacaoCancelamentoResponseDTO;
import com.tccds.sice.modules.comunicacao_cancelamento.entity.ComunicacaoCancelamento;
import com.tccds.sice.modules.comunicacao_cancelamento.repository.ComunicacaoCancelamentoRepository;
import com.tccds.sice.modules.evento.EventoRepository;

import jakarta.persistence.criteria.Predicate;
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
            .map(this::converter)
            .toList();
    }

    /** Carrega o historico apenas quando a tela de detalhes e aberta. */
    @Transactional(readOnly = true)
    public ComunicacaoCancelamentoResponseDTO buscarPorId(Long id) {
        ComunicacaoCancelamento comunicacao = repository.buscarDetalhesPorId(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Comunicacao de cancelamento nao encontrada."));
        return converter(comunicacao);
    }

    @Transactional(readOnly = true)
    public PaginaComunicacoesCancelamentoResponseDTO listarTodos(
            StatusComunicacaoCancelamento status, String busca, Long eventoId, int pagina, int tamanho) {
        String texto = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT);

        // Monta somente os filtros informados, sem depender de um evento selecionado.
        Specification<ComunicacaoCancelamento> filtros = (raiz, consulta, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (eventoId != null) {
                condicoes.add(cb.equal(raiz.get("evento").get("id"), eventoId));
            }
            if (status != null) {
                condicoes.add(cb.equal(raiz.get("status"), status));
            }
            if (!texto.isEmpty()) {
                String padrao = "%" + texto + "%";
                condicoes.add(cb.or(
                    cb.like(cb.lower(raiz.get("evento").get("titulo")), padrao),
                    cb.like(cb.lower(raiz.get("grupoWhatsapp").get("nome")), padrao)
                ));
            }
            return cb.and(condicoes.toArray(Predicate[]::new));
        };

        Page<ComunicacaoCancelamento> resultado = repository.findAll(
            filtros, PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "id")));

        return new PaginaComunicacoesCancelamentoResponseDTO(
            resultado.getContent().stream().map(ResumoComunicacaoCancelamentoResponseDTO::new).toList(),
            resultado.getNumber(), resultado.getSize(),
            resultado.getTotalElements(), resultado.getTotalPages());
    }

    private ComunicacaoCancelamentoResponseDTO converter(ComunicacaoCancelamento comunicacao) {
        return new ComunicacaoCancelamentoResponseDTO(comunicacao,
            tentativaRepository.findByComunicacaoIdOrderByNumeroTentativaAsc(comunicacao.getId()));
    }
}
