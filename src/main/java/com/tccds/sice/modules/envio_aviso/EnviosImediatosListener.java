package com.tccds.sice.modules.envio_aviso;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnviosImediatosListener {
    private final EnvioAvisoService service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void aposCommit(EnviosImediatosEvent evento) {
        try {
            service.processarEnviosPendentesDoEvento(evento.eventoId());
        } catch (Exception exception) {
            // Um erro de comunicacao nao pode fazer o cliente acreditar que
            // o cadastro do evento foi revertido depois de ja ter sido salvo.
            log.error("[SICE-AVISOS] Falha no processamento imediato do evento={}",
                    evento.eventoId(), exception);
        }
    }
}
