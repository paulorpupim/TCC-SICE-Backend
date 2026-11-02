package com.tccds.sice.modules.comunicacao_cancelamento.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tccds.sice.modules.comunicacao_cancelamento.service.ProcessarComunicacaoCancelamentoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component @RequiredArgsConstructor @Slf4j
public class CancelamentoComunicacaoListener {
    private final ProcessarComunicacaoCancelamentoService service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aposCommit(CancelamentoComunicacaoEvent evento) {
        try {
            service.processarDoEvento(evento.eventoId());
        } catch (Exception ex) {
            log.error("[SICE-CANCELAMENTO] Falha apos commit do evento={}", evento.eventoId(), ex);
        }
    }
}
