package com.tccds.sice.modules.cancelamento_aviso;

import java.time.LocalDateTime;

import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tentativa_comunicacao_cancelamento",
       uniqueConstraints = @UniqueConstraint(name = "uk_tentativa_cancelamento_numero",
           columnNames = {"comunicacao_cancelamento_id", "numero_tentativa"}))
@Getter @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class TentativaComunicacaoCancelamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comunicacao_cancelamento_id", nullable = false)
    private ComunicacaoCancelamento comunicacao;

    @Column(name = "numero_tentativa", nullable = false)
    private int numeroTentativa;

    @Column(name = "iniciada_em", nullable = false)
    private LocalDateTime iniciadaEm;

    @Column(name = "finalizada_em", nullable = false)
    private LocalDateTime finalizadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResultadoTentativaEnvioAviso resultado;

    @Column(name = "detalhe_erro", length = 500)
    private String detalheErro;

    public TentativaComunicacaoCancelamento(ComunicacaoCancelamento comunicacao, int numeroTentativa,
                                           LocalDateTime iniciadaEm, LocalDateTime finalizadaEm,
                                           ResultadoTentativaEnvioAviso resultado, String detalheErro) {
        this.comunicacao = comunicacao;
        this.numeroTentativa = numeroTentativa;
        this.iniciadaEm = iniciadaEm;
        this.finalizadaEm = finalizadaEm;
        this.resultado = resultado;
        this.detalheErro = detalheErro;
    }
}
