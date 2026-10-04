package com.tccds.sice.modules.grupo_whatsapp;

import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.turma.Turma;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "grupo_whatsapp",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_grupo_whatsapp_identificador",
            columnNames = "identificador"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class GrupoWhatsapp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private String identificador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoGrupo tipo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", unique = true)
    private Turma turma;

    @Column(nullable = false)
    private boolean ativo = true;

    public GrupoWhatsapp(
            String nome,
            String identificador,
            TipoGrupo tipo,
            Turma turma) {

        this.nome = nome;
        this.identificador = identificador;
        this.tipo = tipo;
        this.turma = turma;
    }
}