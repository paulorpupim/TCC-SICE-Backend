package com.tccds.sice.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tccds.sice.modules.usuario.Usuario;

import lombok.Getter;

@Getter
public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + usuario.getPerfil().name()
                )
        );
    }

    @Override
    public String getPassword() {

        return usuario
                .getCredencial()
                .getSenhaHash();
    }

    @Override
    public String getUsername() {

        return usuario
                .getCredencial()
                .getIdentificador();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {

        return usuario
                .getCredencial()
                .isAtivo();
    }
}
