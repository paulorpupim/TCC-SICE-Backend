package com.tccds.sice.modules.credencial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "tb_credencial")
@Setter
@Getter
@AllArgsConstructor
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
    private boolean primeiroAcesso;

    private boolean ativo;

}
