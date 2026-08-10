package com.tccds.sice.modules.evento;

import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.tccds.sice.modules.usuario.Usuario;
import com.tccds.sice.modules.z_shared.enums.PerfilUsuario;
import com.tccds.sice.modules.z_shared.enums.Serie;

import jakarta.persistence.Enumerated;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_evento")
@Setter
@Getter
@AllArgsConstructor
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(
        name = "tb_evento_serie",
        joinColumns = @JoinColumn(name = "evento_id")
    )
    private Set<Serie> seriesDestinadas;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(
        name = "tb_evento_perfil",
        joinColumns = @JoinColumn(name = "evento_id")
    )
    private Set<PerfilUsuario> perfisDestinados;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    private LocalDateTime atualizadoEm;

    @ManyToOne
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Usuario criadoPor;

}
