package com.tccds.sice.modules.envio_aviso;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EnviosImediatosListener {
    private final EnvioAvisoService service;
    private final Executor executor;

    public EnviosImediatosListener(EnvioAvisoService service,
            @Qualifier("executorEnviosImediatos") Executor executor) {
        this.service = service;
        this.executor = executor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void aposCommit(EnviosImediatosEvent evento) {
        Long eventoId = evento.eventoId();
        try {
            // O cadastro ja foi confirmado. Apenas enfileira o processamento;
            // a resposta HTTP nao espera as chamadas ao bot.
            executor.execute(() -> {
                try {
                    // Continua sequencial, grupo por grupo, com os delays do bot intactos.
                    service.processarEnviosPendentesDoEvento(eventoId);
                } catch (Exception exception) {
                    log.error("[SICE-AVISOS] Falha no processamento imediato do evento={}",
                            eventoId, exception);
                }
            });
        } catch (RuntimeException exception) {
            // Uma fila cheia/parada nao deve transformar um cadastro salvo em erro HTTP.
            // Os avisos PENDENTES continuam elegiveis ao agendador de retentativas.
            log.error("[SICE-AVISOS] Nao foi possivel enfileirar envio imediato do evento={}. "
                    + "Os avisos pendentes serao recuperados pelo agendador.", eventoId, exception);
        }
    }
}
