package com.tccds.sice.security;

import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.Cookie;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Value("${security.jwt.secret}")
        private String jwtSecret;

        @Bean
        public PasswordEncoder passwordEncoder() {

                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder) {

                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
                                userDetailsService);

                provider.setPasswordEncoder(
                                passwordEncoder);

                return new ProviderManager(provider);
        }

        @Bean
        public SecretKey secretKey() {

                byte[] keyBytes = Base64.getDecoder()
                                .decode(jwtSecret);

                return new SecretKeySpec(
                                keyBytes,
                                "HmacSHA256");
        }

        @Bean
        public JwtEncoder jwtEncoder(
                        SecretKey secretKey) {

                return NimbusJwtEncoder
                                .withSecretKey(secretKey)
                                .algorithm(MacAlgorithm.HS256)
                                .build();
        }

        @Bean
        public JwtDecoder jwtDecoder(
                        SecretKey secretKey) {

                return NimbusJwtDecoder
                                .withSecretKey(secretKey)
                                .macAlgorithm(MacAlgorithm.HS256)
                                .build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

                JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();

                authorities.setAuthoritiesClaimName(
                                "roles");

                authorities.setAuthorityPrefix(
                                "ROLE_");

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

                converter.setJwtGrantedAuthoritiesConverter(
                                authorities);

                return converter;
        }

        @Bean
        public BearerTokenResolver bearerTokenResolver() {

                return request -> {

                        Cookie[] cookies = request.getCookies();

                        if (cookies == null) {
                                return null;
                        }

                        for (Cookie cookie : cookies) {

                                if ("token".equals(cookie.getName())) {
                                        return cookie.getValue();
                                }
                        }

                        return null;
                };
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationConverter converter,
                        BearerTokenResolver bearerTokenResolver) throws Exception {

                http
                                .cors(Customizer.withDefaults())

                                .csrf(csrf -> csrf.spa())

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/auth/login")
                                                .permitAll()
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/auth/csrf")
                                                .permitAll()

                                                .anyRequest()
                                                .authenticated())

                                .oauth2ResourceServer(resourceServer -> resourceServer
                                                .bearerTokenResolver(bearerTokenResolver)
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                converter)));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(List.of(
                                "http://localhost:5173"));

                configuration.setAllowedMethods(List.of(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",
                                "DELETE",
                                "OPTIONS"));

                configuration.setAllowedHeaders(List.of(
                                "Authorization",
                                "Content-Type",
                                "X-XSRF-TOKEN"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration(
                                "/**",
                                configuration);

                return source;
        }

}