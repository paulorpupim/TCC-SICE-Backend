package com.tccds.sice.modules.aluno.matricula;

import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.turma.Turma;

import jakarta.persistence.Entity;
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
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    private boolean ativo;

    public Matricula(Aluno aluno, Turma turma, boolean ativo){
        this.aluno = aluno;
        this.turma = turma;
        this.ativo = ativo;
    }

}
