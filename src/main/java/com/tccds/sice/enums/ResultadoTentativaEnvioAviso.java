package com.tccds.sice.enums;

/**
 * Resultado observado pelo SICE, nao uma garantia de entrega no WhatsApp.
 * SEM_CONFIRMACAO inclui erros e respostas incertas da integracao HTTP.
 */
public enum ResultadoTentativaEnvioAviso {
    CONFIRMADO_PELO_BOT,
    SEM_CONFIRMACAO
}
