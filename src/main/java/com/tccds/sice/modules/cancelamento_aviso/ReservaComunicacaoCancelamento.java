package com.tccds.sice.modules.cancelamento_aviso;

import java.time.LocalDateTime;
import com.tccds.sice.modules.bot_whatsapp.dto.EnviarAvisoBotDTO;

public record ReservaComunicacaoCancelamento(Long comunicacaoId, String token,
                                            LocalDateTime iniciadaEm, EnviarAvisoBotDTO mensagem) {}
