package com.tccds.sice.modules.envio_aviso.tentativa_envio_aviso;

import java.time.LocalDateTime;

import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;
import com.tccds.sice.modules.envio_aviso.EnvioAviso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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

/** Uma linha por chamada ao bot, sem sobrescrever tentativas anteriores. */
@Entity
@Table(name = "tentativa_envio_aviso",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_tentativa_envio_numero",
               columnNames = {"envio_aviso_id", "numero_tentativa"}),
       indexes = @Index(name = "idx_tentativa_envio_id", columnList = "envio_aviso_id"))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class TentativaEnvioAviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_aviso_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tentativa_envio_aviso"))
    private EnvioAviso envioAviso;

    @Column(name = "numero_tentativa", nullable = false)
    private Integer numeroTentativa;

    @Column(name = "iniciada_em", nullable = false)
    private LocalDateTime iniciadaEm;

    @Column(name = "finalizada_em", nullable = false)
    private LocalDateTime finalizadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResultadoTentativaEnvioAviso resultado;

    @Column(name = "detalhe_erro", length = 500)
    private String detalheErro;

    public TentativaEnvioAviso(EnvioAviso envioAviso, Integer numeroTentativa,
                              LocalDateTime iniciadaEm, LocalDateTime finalizadaEm,
                              ResultadoTentativaEnvioAviso resultado, String detalheErro) {
        this.envioAviso = envioAviso;
        this.numeroTentativa = numeroTentativa;
        this.iniciadaEm = iniciadaEm;
        this.finalizadaEm = finalizadaEm;
        this.resultado = resultado;
        this.detalheErro = detalheErro;
    }
}
