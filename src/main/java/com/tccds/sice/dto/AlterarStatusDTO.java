package com.tccds.sice.dto;

import jakarta.validation.constraints.NotNull;

public record AlterarStatusDTO(

    @NotNull
    boolean ativo

) {
    
}
