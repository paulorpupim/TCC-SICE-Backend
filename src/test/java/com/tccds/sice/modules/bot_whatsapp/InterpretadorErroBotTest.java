package com.tccds.sice.modules.bot_whatsapp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.tccds.sice.modules.bot_whatsapp.dto.DiagnosticoErroBotDTO;

class InterpretadorErroBotTest {

    private static void codigo(String erro, String esperado) {
        DiagnosticoErroBotDTO diagnostico = InterpretadorErroBot.interpretar(erro);
        assertNotNull(diagnostico);
        assertEquals(esperado, diagnostico.codigo());
        assertFalse(diagnostico.mensagem().isBlank());
        assertFalse(diagnostico.orientacao().isBlank());
    }

    @Test
    void semErroNaoCriaDiagnostico() {
        assertNull(InterpretadorErroBot.interpretar(null));
        assertNull(InterpretadorErroBot.interpretar("  "));
    }

    @Test
    void reconheceMensagensReaisDoBot() {
        codigo("503 Service Unavailable: '{\"sucesso\":false,\"erro\":\"WhatsApp ainda não está conectado.\"}'",
                "WHATSAPP_DESCONECTADO");
        codigo("401 Unauthorized: '{\"sucesso\":false,\"erro\":\"Chave inválida.\"}'",
                "AUTENTICACAO_BOT");
        codigo("400 Bad Request: '{\"erro\":\"identificadorGrupo e mensagem são obrigatórios.\"}'",
                "DADOS_INVALIDOS");
        codigo("500 Internal Server Error: '{\"erro\":\"O identificador informado não pertence a um grupo.\"}'",
                "GRUPO_INVALIDO");
    }

    @Test
    void distingueFalhasDeConexaoEResultadoIncerto() {
        codigo("I/O error on POST request: Connection refused", "CONEXAO_BOT");
        codigo("I/O error: Read timed out", "TEMPO_ESGOTADO");
        codigo("RESULTADO INCERTO: o processo foi interrompido sem confirmar o envio.", "RESULTADO_INCERTO");
        codigo("RESULTADO INCERTO: nao ha confirmacao da mensagem de cancelamento.", "RESULTADO_INCERTO");
        codigo("Cancelamento nao enviado: horario do evento ja passou.", "COMUNICACAO_NAO_ELEGIVEL");
        codigo("503 Service Unavailable", "BOT_INDISPONIVEL");
        codigo("500 Internal Server Error", "ERRO_INTERNO_BOT");
    }

    @Test
    void respostaNaoConhecidaNaoInventaCausa() {
        codigo("erro antigo personalizado sem codigo", "ERRO_NAO_IDENTIFICADO");
        var resposta = InterpretadorErroBot.interpretar("I/O error: Read timed out");
        assertTrue(resposta.orientacao().contains("Não é possível confirmar"));
    }
}
