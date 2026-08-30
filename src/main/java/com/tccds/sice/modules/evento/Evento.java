package com.tccds.sice.modules.evento;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.ModalidadeEnsino;
import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.modules.evento.evento_turma.EventoTurma;
import com.tccds.sice.modules.usuario.Usuario;

import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
    private LocalDateTime dataHoraInicio;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusEvento status;

    @ElementCollection(targetClass = PerfilUsuario.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "evento_perfil", joinColumns = @JoinColumn(name = "evento_id"))
    @Column(nullable = false)
    private Set<PerfilUsuario> perfisDestinados = new HashSet<>();

    @ElementCollection(targetClass = Etapa.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "evento_etapa", joinColumns = @JoinColumn(name = "evento_id"))
    @Column(nullable = false)
    private Set<Etapa> etapasDestinadas = new HashSet<>();

    @ElementCollection(targetClass = ModalidadeEnsino.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "evento_modalidade", joinColumns = @JoinColumn(name = "evento_id"))
    @Column(nullable = false)
    private Set<ModalidadeEnsino> modalidadesDestinadas = new HashSet<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventoTurma> destinacoesTurma = new HashSet<>();

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
            LocalDateTime dataHoraInicio,
            StatusEvento status,
            Set<PerfilUsuario> perfisDestinados,
            Set<Etapa> etapasDestinadas,
            Set<ModalidadeEnsino> modalidadesDestinadas,
            Usuario criadoPor) {

        this.titulo = titulo;
        this.descricao = descricao;
        this.dataHoraInicio = dataHoraInicio;
        this.status = status;
        this.perfisDestinados = perfisDestinados;
        this.etapasDestinadas = etapasDestinadas;
        this.modalidadesDestinadas = modalidadesDestinadas;
        this.criadoPor = criadoPor;
    }

}
