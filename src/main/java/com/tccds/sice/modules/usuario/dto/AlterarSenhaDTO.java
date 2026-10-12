package com.tccds.sice.modules.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaDTO(

        @NotBlank
        String senhaAtual,

        @NotBlank
        @Size(min = 6)
        String novaSenha
) {
}