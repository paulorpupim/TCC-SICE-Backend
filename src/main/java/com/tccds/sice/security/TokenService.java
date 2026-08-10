package com.tccds.sice.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.tccds.sice.modules.usuario.Usuario;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.expiration-seconds}")
    private long expirationSeconds;


    public String gerarToken(
            Usuario usuario
    ) {

        Instant agora =
                Instant.now();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()

                        .subject(
                                usuario
                                        .getCredencial()
                                        .getIdentificador()
                        )

                        .issuedAt(agora)

                        .expiresAt(
                                agora.plus(
                                        expirationSeconds,
                                        ChronoUnit.SECONDS
                                )
                        )

                        .claim(
                                "roles",
                                List.of(
                                        usuario
                                                .getPerfil()
                                                .name()
                                )
                        )

                        .build();


        JwsHeader header =
                JwsHeader
                        .with(MacAlgorithm.HS256)
                        .build();


        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();
    }
}