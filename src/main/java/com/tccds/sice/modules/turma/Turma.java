package com.tccds.sice.modules.turma;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.modules.curso.Curso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anoLetivo;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Etapa etapa;
    
    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalidadeEnsino modalidade;

    private boolean ativo = true;

    public Turma(Integer anoLetivo, Etapa etapa, ModalidadeEnsino modalidade, Curso curso ){
        this.anoLetivo = anoLetivo;
        this.etapa = etapa;
        this.modalidade = modalidade;
        this.curso = curso;
    }

}
