package com.tccds.sice.modules.evento.evento_destino;

import java.util.HashSet;
import java.util.Set;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.evento_destino_turma.EventoDestinoTurma;
import com.tccds.sice.modules.turma.Turma;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "evento_destino",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_evento_destino_perfil",
            columnNames = {"evento_id", "perfil"}
        )
    }
)
@Setter
@Getter
@NoArgsConstructor
public class EventoDestino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfilUsuario perfil;

    @ElementCollection(targetClass = Etapa.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "evento_destino_etapa",
        joinColumns = @JoinColumn(name = "evento_destino_id")
    )
    @Column(name = "etapa", nullable = false)
    private Set<Etapa> etapas = new HashSet<>();

    @ElementCollection(targetClass = ModalidadeEnsino.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "evento_destino_modalidade",
        joinColumns = @JoinColumn(name = "evento_destino_id")
    )
    @Column(name = "modalidade", nullable = false)
    private Set<ModalidadeEnsino> modalidades = new HashSet<>();

    @OneToMany(
        mappedBy = "eventoDestino",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<EventoDestinoTurma> destinacoesTurma = new HashSet<>();

    public EventoDestino(
        PerfilUsuario perfil,
        Set<Etapa> etapas,
        Set<ModalidadeEnsino> modalidades
    ) {
        this.perfil = perfil;

        this.etapas = etapas != null
                ? new HashSet<>(etapas)
                : new HashSet<>();

        this.modalidades = modalidades != null
                ? new HashSet<>(modalidades)
                : new HashSet<>();
    }

    public void adicionarTurma(Turma turma) {
        EventoDestinoTurma destinacao =
                new EventoDestinoTurma(this, turma);

        destinacoesTurma.add(destinacao);
    }

    public void removerTurma(Turma turma) {
        destinacoesTurma.removeIf(
            destinacao ->
                destinacao.getTurma().getId().equals(turma.getId())
        );
    }
}