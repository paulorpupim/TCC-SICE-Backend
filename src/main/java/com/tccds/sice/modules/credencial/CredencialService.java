package com.tccds.sice.modules.credencial;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CredencialService {
    
    private final PasswordEncoder passwordEncoder;

    public Credencial criar(String identificador, String senha){
        return new Credencial(
            identificador,
            passwordEncoder.encode(senha)
        );
    }

}
