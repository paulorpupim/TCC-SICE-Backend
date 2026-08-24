package com.tccds.sice.modules.evento.evento_turma;

import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.turma.Turma;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_evento_turma")
@Getter
@Setter
@NoArgsConstructor
public class EventoTurma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    public EventoTurma(Evento evento, Turma turma){
        this.evento = evento;
        this.turma = turma;
    }

}
