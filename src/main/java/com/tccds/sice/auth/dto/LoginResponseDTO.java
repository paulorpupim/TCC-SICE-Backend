package com.tccds.sice.auth.dto;

import com.tccds.sice.enums.PerfilUsuario;

public record LoginResponseDTO(
    PerfilUsuario perfil,
    boolean primeiroAcesso
) {
    
}
