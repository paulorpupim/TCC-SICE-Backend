package com.tccds.sice.auth;

import com.tccds.sice.enums.PerfilUsuario;

public record LoginResult(
    String token,
    String nome,
    PerfilUsuario perfil,
    boolean primeiroAcesso
) {
    
}
