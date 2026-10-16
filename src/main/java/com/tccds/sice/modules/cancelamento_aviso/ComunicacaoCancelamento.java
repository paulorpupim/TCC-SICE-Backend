package com.tccds.sice.modules.cancelamento_aviso;

import java.time.LocalDateTime;

import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** Nunca altera nem substitui o EnvioAviso original; conteudo e destinatario sao fotografados. */
@Entity
@Table(name = "comunicacao_cancelamento",
       uniqueConstraints = @UniqueConstraint(name = "uk_cancelamento_evento_grupo",
           columnNames = {"evento_id", "grupo_whatsapp_id"}),
       indexes = @Index(name = "idx_cancelamento_evento", columnList = "evento_id"))
@Getter @Setter @NoArgsConstructor
public class ComunicacaoCancelamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_whatsapp_id", nullable = false)
    private GrupoWhatsapp grupoWhatsapp;

    @Column(name = "envio_original_id", nullable = false)
    private Long envioOriginalId;

    @Column(name = "identificador_grupo", nullable = false, length = 255)
    private String identificadorGrupo;

    @Column(name = "mensagem", nullable = false, length = 4000)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusComunicacaoCancelamento status = StatusComunicacaoCancelamento.PENDENTE;

    @Column(nullable = false)
    private int tentativas = 0;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "processando_desde")
    private LocalDateTime processandoDesde;

    @Column(name = "reserva_token", length = 36)
    private String reservaToken;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    @Column(name = "ultimo_erro", length = 500)
    private String ultimoErro;

    public ComunicacaoCancelamento(Evento evento, GrupoWhatsapp grupo, Long envioOriginalId,
                                  String mensagem, LocalDateTime criadoEm) {
        this.evento = evento;
        this.grupoWhatsapp = grupo;
        this.envioOriginalId = envioOriginalId;
        this.identificadorGrupo = grupo.getIdentificador();
        this.mensagem = mensagem;
        this.criadoEm = criadoEm;
    }
}
