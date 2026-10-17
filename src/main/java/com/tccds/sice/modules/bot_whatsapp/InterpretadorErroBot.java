package com.tccds.sice.modules.bot_whatsapp;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import com.tccds.sice.modules.bot_whatsapp.dto.DiagnosticoErroBotDTO;

/**
 * Interpreta erros JA registrados, inclusive historicos anteriores.
 * Nao dispara envios, nao altera banco e nao determina se houve entrega no WhatsApp.
 */
public final class InterpretadorErroBot {
    private static final String RESULTADO_INCERTO =
            "Não é possível confirmar se a mensagem chegou ao WhatsApp. Confira o histórico antes de qualquer reenvio manual.";
    private static final Pattern HTTP_400 = Pattern.compile("\\b400\\b");
    private static final Pattern HTTP_401 = Pattern.compile("\\b40[13]\\b");
    private static final Pattern HTTP_404 = Pattern.compile("\\b404\\b");
    private static final Pattern HTTP_429 = Pattern.compile("\\b429\\b");
    private static final Pattern HTTP_500 = Pattern.compile("\\b50[01234]\\b");
    private static final Pattern HTTP_503 = Pattern.compile("\\b503\\b");
    private static final Pattern HTTP_504 = Pattern.compile("\\b504\\b");

    private InterpretadorErroBot() {
    }

    /** null significa que o registro nao possui erro para interpretar. */
    public static DiagnosticoErroBotDTO interpretar(String erroTecnico) {
        if (erroTecnico == null || erroTecnico.isBlank()) return null;

        String texto = Normalizer.normalize(erroTecnico, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);

        if (tem(texto, "resultado incerto", "reserva expirad", "processo foi interrompido")) {
            return novo("RESULTADO_INCERTO", "A tentativa terminou sem confirmação do bot.", RESULTADO_INCERTO);
        }
        if (tem(texto, "evento ou grupo deixou de ser elegivel", "horario do evento ja passou")) {
            return novo("COMUNICACAO_NAO_ELEGIVEL", "A comunicação não foi realizada porque o evento ou o grupo deixou de ser elegível.",
                    "Confira o evento, o grupo e o horário programado. Nenhum reenvio deve ser feito sem revisar a situação.");
        }
        if (tem(texto, "nao pertence a um grupo", "nao e um grupo", "isgroup", "group not found", "chat not found")) {
            return novo("GRUPO_INVALIDO", "O bot não conseguiu reconhecer o destino como um grupo do WhatsApp.",
                    "Confira o identificador do grupo cadastrado e se o bot ainda possui acesso a ele. " + RESULTADO_INCERTO);
        }
        if (tem(texto, "chave invalida", "unauthorized", "forbidden") || HTTP_401.matcher(texto).find()) {
            return novo("AUTENTICACAO_BOT", "O SICE não conseguiu se autenticar no bot.",
                    "Solicite a verificação da chave de integração entre o backend e o bot. Não exponha essa chave na interface.");
        }
        if (tem(texto, "identificadorgrupo e mensagem", "bad request") || HTTP_400.matcher(texto).find()) {
            return novo("DADOS_INVALIDOS", "O bot recusou os dados informados para o envio.",
                    "Confira a configuração do grupo e solicite a verificação dos dados enviados pelo SICE.");
        }
        if (tem(texto, "whatsapp ainda nao esta conectado", "whatsapp desconectado", "whatsapp nao esta conectado")) {
            return novo("WHATSAPP_DESCONECTADO", "O WhatsApp do bot parece estar desconectado ou ainda não está pronto.",
                    "Verifique a conexão e a autenticação do WhatsApp no bot. " + RESULTADO_INCERTO);
        }
        if (tem(texto, "connection refused", "econnrefused", "connectexception", "connection reset", "no route to host")) {
            return novo("CONEXAO_BOT", "O SICE não conseguiu estabelecer uma conexão com o bot.",
                    "Confira se o serviço do bot está ligado, seu endereço e a rede. " + RESULTADO_INCERTO);
        }
        if (tem(texto, "timed out", "timeout", "tempo limite", "sockettimeoutexception") || HTTP_504.matcher(texto).find()) {
            return novo("TEMPO_ESGOTADO", "O bot não respondeu dentro do tempo esperado.",
                    "Verifique a disponibilidade do bot e a conexão de rede. " + RESULTADO_INCERTO);
        }
        if (HTTP_503.matcher(texto).find() || tem(texto, "service unavailable")) {
            return novo("BOT_INDISPONIVEL", "O bot está temporariamente indisponível ou ainda não está pronto.",
                    "Confira o estado do bot e a conexão com o WhatsApp. " + RESULTADO_INCERTO);
        }
        if (HTTP_429.matcher(texto).find() || tem(texto, "too many requests")) {
            return novo("LIMITE_REQUISICOES", "O bot ou outro serviço limitou temporariamente as requisições.",
                    "Aguarde a normalização do serviço e confira as tentativas antes de qualquer reenvio.");
        }
        if (HTTP_404.matcher(texto).find() || tem(texto, "not found")) {
            return novo("RECURSO_NAO_ENCONTRADO", "O bot não encontrou o recurso solicitado.",
                    "Confira o endereço da integração e os dados do grupo. " + RESULTADO_INCERTO);
        }
        if (HTTP_500.matcher(texto).find() || tem(texto, "internal server error")) {
            return novo("ERRO_INTERNO_BOT", "O bot encontrou uma falha ao processar a mensagem.",
                    "Verifique os registros técnicos do bot para identificar a causa. " + RESULTADO_INCERTO);
        }
        return novo("ERRO_NAO_IDENTIFICADO", "O SICE não conseguiu confirmar o resultado desta tentativa.",
                "Consulte os detalhes técnicos para investigar a causa. " + RESULTADO_INCERTO);
    }

    private static boolean tem(String texto, String... trechos) {
        for (String trecho : trechos) {
            if (texto.contains(trecho)) return true;
        }
        return false;
    }

    private static DiagnosticoErroBotDTO novo(String codigo, String mensagem, String orientacao) {
        return new DiagnosticoErroBotDTO(codigo, mensagem, orientacao);
    }
}
