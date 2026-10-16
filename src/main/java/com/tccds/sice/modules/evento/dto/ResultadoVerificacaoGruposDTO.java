package com.tccds.sice.modules.evento.dto;

import java.util.List;

import com.tccds.sice.modules.envio_aviso.ResolverGruposAvisoService;
import com.tccds.sice.modules.envio_aviso.ResolverGruposAvisoService.DestinoSemGrupo;

public record ResultadoVerificacaoGruposDTO(
    int totalGruposAtivos,
    List<DestinoSemGrupo> destinosSemGrupo
) {
    public ResultadoVerificacaoGruposDTO(ResolverGruposAvisoService.Resultado resultado) {
        this(resultado.grupos().size(), resultado.semGrupo());
    }
}
