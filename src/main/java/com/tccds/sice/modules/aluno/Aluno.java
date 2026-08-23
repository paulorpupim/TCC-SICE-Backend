package com.tccds.sice.modules.aluno;

import java.util.HashSet;
import java.util.Set;

import com.tccds.sice.modules.aluno.matricula.Matricula;
import com.tccds.sice.modules.usuario.Usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_aluno")
@Setter
@Getter
@NoArgsConstructor
public class Aluno {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id",nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "aluno")
    private Set<Matricula> matriculas = new HashSet<>(); 

    public Aluno(Usuario usuario){
        this.usuario = usuario;
    }

}
