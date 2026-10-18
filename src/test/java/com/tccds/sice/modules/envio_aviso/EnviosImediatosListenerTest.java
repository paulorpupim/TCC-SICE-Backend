package com.tccds.sice.modules.envio_aviso;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnviosImediatosListenerTest {
    @Mock EnvioAvisoService service;
    @Mock Executor executor;

    @Test
    void cadastroSomenteEnfileiraSemEsperarOBot() {
        var listener = new EnviosImediatosListener(service, executor);

        listener.aposCommit(new EnviosImediatosEvent(77L));

        // Nenhuma chamada ao bot/processamento ocorre na thread do cadastro.
        verifyNoInteractions(service);
        ArgumentCaptor<Runnable> tarefa = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(tarefa.capture());

        tarefa.getValue().run();
        verify(service).processarEnviosPendentesDoEvento(77L);
    }

    @Test
    void erroDuranteEnvioAssincronoNaoPropagaParaCadastro() {
        var listener = new EnviosImediatosListener(service, executor);
        doThrow(new IllegalStateException("bot indisponivel"))
                .when(service).processarEnviosPendentesDoEvento(77L);

        listener.aposCommit(new EnviosImediatosEvent(77L));
        ArgumentCaptor<Runnable> tarefa = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(tarefa.capture());

        assertDoesNotThrow(() -> tarefa.getValue().run());
    }

    @Test
    void filaCheiaNaoFazCadastroConfirmadoParecerFalha() {
        var listener = new EnviosImediatosListener(service, executor);
        doThrow(new RejectedExecutionException("fila cheia"))
                .when(executor).execute(any(Runnable.class));

        assertDoesNotThrow(() -> listener.aposCommit(new EnviosImediatosEvent(77L)));
        verifyNoInteractions(service);
    }
}
