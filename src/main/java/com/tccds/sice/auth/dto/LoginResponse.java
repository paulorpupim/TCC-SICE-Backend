package com.tccds.sice.auth.dto;

import com.tccds.sice.modules.z_shared.enums.PerfilUsuario;

public record LoginResponse(
    String token,
    String tipo,
    PerfilUsuario perfil,
    boolean primeiroAcesso
) {
    
}
