package com.tccds.sice.auth;

import com.tccds.sice.enums.PerfilUsuario;

public record LoginResult(
    String token,
    PerfilUsuario perfil,
    boolean primeiroAcesso
) {
    
}
