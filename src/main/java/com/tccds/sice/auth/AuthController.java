package com.tccds.sice.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tccds.sice.auth.dto.LoginRequestDTO;
import com.tccds.sice.auth.dto.LoginResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;

        @PostMapping("/login")
        public ResponseEntity<LoginResponseDTO> login(
                        @RequestBody LoginRequestDTO request) {

                LoginResult result = authService.login(request);

                ResponseCookie cookie = ResponseCookie
                                .from("token", result.token())
                                .httpOnly(true)
                                .secure(false)
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(60 * 60) // Idealmente colocar o tempo de expiração do token
                                .build();

                LoginResponseDTO response = new LoginResponseDTO(
                                result.perfil(),
                                result.primeiroAcesso());

                return ResponseEntity
                                .ok()
                                .header(
                                                HttpHeaders.SET_COOKIE,
                                                cookie.toString())
                                .body(response);
        }

        @PostMapping("/logout")
        public ResponseEntity<Void> logout() {

                ResponseCookie cookie = ResponseCookie
                                .from("token", "")
                                .httpOnly(true)
                                .secure(false)
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(0)
                                .build();

                return ResponseEntity
                                .noContent()
                                .header(
                                                HttpHeaders.SET_COOKIE,
                                                cookie.toString())
                                .build();
        }

        @GetMapping("/me")
        public ResponseEntity<LoginResponseDTO> me(
                        @AuthenticationPrincipal Jwt jwt) {

                return ResponseEntity.ok(
                                authService.me(jwt.getSubject()));
        }

        @GetMapping("/csrf")
        public CsrfToken csrf(CsrfToken csrfToken) {
                return csrfToken;
        }

}