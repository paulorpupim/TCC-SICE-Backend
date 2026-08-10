package com.tccds.sice.auth.dto;

public record LoginRequest(
    String identificador,
    String senha
) {
    
}
