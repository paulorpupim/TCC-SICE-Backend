package com.tccds.sice.modules.envio_aviso;

import java.time.LocalDateTime;
import com.tccds.sice.modules.bot_whatsapp.dto.EnviarAvisoBotDTO;

public record ReservaEnvioAviso(
        Long envioId,
        String token,
        int numeroTentativa,
        LocalDateTime iniciadaEm,
        EnviarAvisoBotDTO mensagem) {
}
