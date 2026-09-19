package com.tccds.sice.modules.evento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.evento.evento_destino.EventoDestino;
import com.tccds.sice.modules.usuario.Usuario;

import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusEvento status = StatusEvento.ATIVO;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventoDestino> destinos = new HashSet<>();

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    private LocalDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Usuario criadoPor;

    public Evento(
            String titulo,
            String descricao,
            LocalDate dataInicio,
            LocalTime horaInicio,
            Usuario criadoPor) {

        this.titulo = titulo;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.criadoPor = criadoPor;
    }

    public void adicionarDestino(EventoDestino destino) {
        destinos.add(destino);
        destino.setEvento(this);
    }

    public void removerDestino(EventoDestino destino) {
        destinos.remove(destino);
        destino.setEvento(null);
    }

}
