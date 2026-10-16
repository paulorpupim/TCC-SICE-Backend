package com.tccds.sice.modules.envio_aviso;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnviosImediatosListenerTest {
    @Mock EnvioAvisoService service;

    @Test
    void listenerDelegaProcessamentoAoServico() {
        var listener = new EnviosImediatosListener(service);
        listener.aposCommit(new EnviosImediatosEvent(77L));
        verify(service).processarEnviosPendentesDoEvento(77L);
    }

    @Test
    void erroDeEnvioNaoPropagaExcecaoParaCadastroJaConfirmado() {
        var listener = new EnviosImediatosListener(service);
        doThrow(new IllegalStateException("bot indisponivel"))
                .when(service).processarEnviosPendentesDoEvento(77L);
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> listener.aposCommit(new EnviosImediatosEvent(77L)));
    }
}
