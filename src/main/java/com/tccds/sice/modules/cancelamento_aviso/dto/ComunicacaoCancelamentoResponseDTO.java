package com.tccds.sice.modules.cancelamento_aviso.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tccds.sice.modules.bot_whatsapp.InterpretadorErroBot;
import com.tccds.sice.modules.bot_whatsapp.dto.DiagnosticoErroBotDTO;
import com.tccds.sice.modules.cancelamento_aviso.ComunicacaoCancelamento;
import com.tccds.sice.modules.cancelamento_aviso.StatusComunicacaoCancelamento;
import com.tccds.sice.modules.cancelamento_aviso.TentativaComunicacaoCancelamento;
import com.tccds.sice.enums.ResultadoTentativaEnvioAviso;

public record ComunicacaoCancelamentoResponseDTO(
        Long id, Long eventoId, Long grupoId, Long envioOriginalId, String identificadorGrupo,
        String mensagem, StatusComunicacaoCancelamento status,
        int tentativas, LocalDateTime criadoEm, LocalDateTime enviadoEm,
        String ultimoErro, DiagnosticoErroBotDTO diagnosticoErro, List<TentativaDTO> historico) {
    public record TentativaDTO(int numero, LocalDateTime inicio, LocalDateTime fim,
                               ResultadoTentativaEnvioAviso resultado, String erro, DiagnosticoErroBotDTO diagnosticoErro) {
        public TentativaDTO(TentativaComunicacaoCancelamento tentativa) {
            this(tentativa.getNumeroTentativa(), tentativa.getIniciadaEm(),
                 tentativa.getFinalizadaEm(), tentativa.getResultado(), tentativa.getDetalheErro(),
                    InterpretadorErroBot.interpretar(tentativa.getDetalheErro()));
        }
    }

    public ComunicacaoCancelamentoResponseDTO(ComunicacaoCancelamento comunicacao,
                                             List<TentativaComunicacaoCancelamento> tentativas) {
        this(comunicacao.getId(), comunicacao.getEvento().getId(), comunicacao.getGrupoWhatsapp().getId(),
             comunicacao.getEnvioOriginalId(), comunicacao.getIdentificadorGrupo(),
             comunicacao.getMensagem(), comunicacao.getStatus(), comunicacao.getTentativas(),
             comunicacao.getCriadoEm(), comunicacao.getEnviadoEm(), comunicacao.getUltimoErro(),
             InterpretadorErroBot.interpretar(comunicacao.getUltimoErro()), tentativas.stream().map(TentativaDTO::new).toList());
    }
}
