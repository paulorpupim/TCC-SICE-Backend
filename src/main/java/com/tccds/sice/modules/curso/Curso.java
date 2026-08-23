package com.tccds.sice.modules.curso;

import com.tccds.sice.enums.ModalidadeEnsino;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_curso")
@Setter
@Getter
@NoArgsConstructor
public class Curso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadeEnsino modalidade;

    public Curso(String nome, ModalidadeEnsino modalidade){
        this.nome = nome;
        this.modalidade = modalidade;
    }

}
