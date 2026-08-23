package com.tccds.sice.auth.dto;

import com.tccds.sice.enums.PerfilUsuario;

public record LoginResponse(
    String token,
    String tipo,
    PerfilUsuario perfil,
    boolean primeiroAcesso
) {
    
}
