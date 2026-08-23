package com.tccds.sice.modules.usuario;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.credencial.Credencial;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_usuario")
@Setter
@Getter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfilUsuario perfil;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "credencial_id", nullable = false, unique = true)
    private Credencial credencial;

    public Usuario(String nome, String email, PerfilUsuario perfil, Credencial credencial){
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.credencial = credencial;
    }

}
