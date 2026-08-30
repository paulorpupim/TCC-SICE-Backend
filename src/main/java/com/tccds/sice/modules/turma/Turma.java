package com.tccds.sice.modules.turma;

import com.tccds.sice.enums.Etapa;
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

    @Column(nullable = false, length = 4)
    private Integer anoLetivo;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Etapa etapa;
    
    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    private Boolean ativo;

    public Turma(Integer anoLetivo, Etapa etapa, Curso curso ){
        this.anoLetivo = anoLetivo;
        this.etapa = etapa;
        this.curso = curso;
        this.ativo = true;
    }

}
