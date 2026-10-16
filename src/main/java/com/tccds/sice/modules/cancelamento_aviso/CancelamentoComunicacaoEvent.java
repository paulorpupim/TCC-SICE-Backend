package com.tccds.sice.modules.cancelamento_aviso;

/** Disparado apenas apos commit bem-sucedido do cancelamento. */
public record CancelamentoComunicacaoEvent(Long eventoId) {}
