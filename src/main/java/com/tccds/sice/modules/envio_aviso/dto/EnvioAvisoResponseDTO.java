package com.tccds.sice.modules.envio_aviso.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.tccds.sice.modules.bot_whatsapp.InterpretadorErroBot;
import com.tccds.sice.modules.bot_whatsapp.dto.DiagnosticoErroBotDTO;
import com.tccds.sice.enums.StatusEnvioAviso;
import com.tccds.sice.enums.StatusEvento;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.envio_aviso.EnvioAviso;

public record EnvioAvisoResponseDTO(
        Long id,
        Long eventoId,
        String eventoTitulo,
        StatusEvento eventoStatus,
        LocalDate eventoDataInicio,
        LocalTime eventoHoraInicio,
        LocalDate dataAviso,
        Long grupoId,
        String grupoNome,
        TipoGrupo grupoTipo,
        boolean grupoAtivo,
        StatusEnvioAviso status,
        Integer tentativas,
        LocalDateTime ultimaTentativaEm,
        LocalDateTime enviadoEm,
        String ultimoErro,
        DiagnosticoErroBotDTO diagnosticoErro
) {
    public EnvioAvisoResponseDTO(EnvioAviso envio) {
        this(
                envio.getId(),
                envio.getEvento().getId(),
                envio.getEvento().getTitulo(),
                envio.getEvento().getStatus(),
                envio.getEvento().getDataInicio(),
                envio.getEvento().getHoraInicio(),
                envio.getEvento().getDataAviso(),
                envio.getGrupoWhatsapp().getId(),
                envio.getGrupoWhatsapp().getNome(),
                envio.getGrupoWhatsapp().getTipo(),
                envio.getGrupoWhatsapp().isAtivo(),
                envio.getStatus(),
                envio.getTentativas(),
                envio.getUltimaTentativaEm(),
                envio.getEnviadoEm(),
                envio.getUltimoErro(),
                InterpretadorErroBot.interpretar(envio.getUltimoErro())
        );
    }
}
