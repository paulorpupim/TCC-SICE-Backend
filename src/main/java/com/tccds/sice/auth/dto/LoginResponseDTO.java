package com.tccds.sice.auth.dto;

import com.tccds.sice.enums.PerfilUsuario;

public record LoginResponseDTO(
    String nome,
    PerfilUsuario perfil,
    boolean primeiroAcesso
) {
    
}
