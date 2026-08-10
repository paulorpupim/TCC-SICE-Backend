package com.tccds.sice.modules.aluno;

import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.z_shared.enums.Serie;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_aluno")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Aluno {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id",nullable = false, unique = true)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @JoinColumn(nullable = false)
    private Serie serie;

}
