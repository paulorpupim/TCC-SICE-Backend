package com.tccds.sice.modules.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Senha provisória informada pelo administrador, nunca devolvida pela API. */
public record RedefinirSenhaUsuarioDTO(
        @NotBlank(message = "Informe uma senha provisória.")
        @Size(min = 6, max = 72, message = "A senha provisória deve ter entre 6 e 72 caracteres.")
        String senhaTemporaria
) {
}
