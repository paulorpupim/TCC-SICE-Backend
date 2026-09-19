package com.tccds.sice.modules.evento.evento_destino_turma;

import com.tccds.sice.modules.evento.evento_destino.EventoDestino;
import com.tccds.sice.modules.turma.Turma;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "evento_destino_turma", indexes = {
        @Index(name = "idx_evento_destino_turma_destino", columnList = "evento_destino_id"),
        @Index(name = "idx_evento_destino_turma_turma", columnList = "turma_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_evento_destino_turma", columnNames = { "evento_destino_id", "turma_id" })
})
@Getter
@Setter
@NoArgsConstructor
public class EventoDestinoTurma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_destino_id", nullable = false)
    private EventoDestino eventoDestino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    public EventoDestinoTurma(EventoDestino eventoDestino, Turma turma) {
        this.eventoDestino = eventoDestino;
        this.turma = turma;
    }

}
