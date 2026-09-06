package com.tccds.sice.modules.credencial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Setter
@Getter
@NoArgsConstructor
public class Credencial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String identificador;

    @Column(nullable = false, length = 255)
    private String senhaHash;

    @Column(nullable = false)
    private boolean primeiroAcesso = true;

    private boolean ativo = true;

    public Credencial(String identificador, String senha){
        this.identificador = identificador;
        this.senhaHash = senha;
    }

}
