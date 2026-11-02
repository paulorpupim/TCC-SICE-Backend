package com.tccds.sice.modules.comunicacao_cancelamento.event;

/** Disparado apenas apos commit bem-sucedido do cancelamento. */
public record CancelamentoComunicacaoEvent(
    Long eventoId
) {
    
}
