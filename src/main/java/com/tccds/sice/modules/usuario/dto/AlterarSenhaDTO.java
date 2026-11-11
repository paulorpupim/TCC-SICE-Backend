package com.tccds.sice.modules.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AlterarSenhaDTO(

        @NotBlank(message = "Informe sua senha atual.")
        String senhaAtual,

        @NotBlank(message = "Informe a nova senha.")
        @Size(min = 6, max = 72, message = "A nova senha deve ter entre 6 e 72 caracteres.")
        @Pattern(
                regexp = "(?s)(?=.*\\p{Lu})(?=.*\\p{Ll})(?=.*[0-9])(?=.*[^\\p{L}\\p{N}\\s]).+",
                message = "A nova senha deve conter letra maiúscula, minúscula, número e caractere especial."
        )
        String novaSenha
) {
}
