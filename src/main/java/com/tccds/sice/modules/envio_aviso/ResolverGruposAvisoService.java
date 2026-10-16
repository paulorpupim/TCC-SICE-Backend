package com.tccds.sice.modules.envio_aviso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.evento_destino_turma.dto.CriarEventoDestinoDTO;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsappRepository;

import lombok.RequiredArgsConstructor;

/**
 * A mesma regra de grupos vale tanto para o envio real quanto para a pre-visualizacao.
 * Etapas e modalidades, sem turma explicita, utilizam o grupo geral de alunos:
 * essa e a regra atual da escola e nao deve ser alterada pela verificacao.
 */
@Service
@RequiredArgsConstructor
public class ResolverGruposAvisoService {

    private final GrupoWhatsappRepository grupoWhatsappRepository;

    public record DestinoSemGrupo(PerfilUsuario perfil, Long turmaId) {}

    public record Resultado(Set<GrupoWhatsapp> grupos, List<DestinoSemGrupo> semGrupo) {}

    public Resultado resolver(Evento evento) {
        ResultadoAcumulado acumulado = new ResultadoAcumulado();
        evento.getDestinos().forEach(destino -> {
            Set<Long> turmasIds = destino.getDestinacoesTurma().stream()
                .map(destinacao -> destinacao.getTurma().getId())
                .collect(Collectors.toSet());
            adicionarDestino(destino.getPerfil(), turmasIds, acumulado);
        });
        return acumulado.resultado();
    }

    public Resultado resolver(Set<CriarEventoDestinoDTO> destinos) {
        ResultadoAcumulado acumulado = new ResultadoAcumulado();
        destinos.forEach(destino -> adicionarDestino(
            destino.perfil(), destino.turmasIds(), acumulado));
        return acumulado.resultado();
    }

    private void adicionarDestino(PerfilUsuario perfil, Set<Long> turmasIds, ResultadoAcumulado resultado) {
        if (perfil == PerfilUsuario.PROFESSOR) {
            grupoWhatsappRepository.findByTipoAndAtivoTrue(TipoGrupo.GERAL_PROFESSORES)
                .ifPresentOrElse(resultado.grupos::add,
                    () -> resultado.semGrupo.add(new DestinoSemGrupo(PerfilUsuario.PROFESSOR, null)));
            return;
        }
        if (perfil != PerfilUsuario.ALUNO) {
            return;
        }
        if (turmasIds == null || turmasIds.isEmpty()) {
            grupoWhatsappRepository.findByTipoAndAtivoTrue(TipoGrupo.GERAL_ALUNOS)
                .ifPresentOrElse(resultado.grupos::add,
                    () -> resultado.semGrupo.add(new DestinoSemGrupo(PerfilUsuario.ALUNO, null)));
            return;
        }
        for (Long turmaId : turmasIds) {
            grupoWhatsappRepository.findByTurmaIdAndAtivoTrue(turmaId)
                .ifPresentOrElse(resultado.grupos::add,
                    () -> resultado.semGrupo.add(new DestinoSemGrupo(PerfilUsuario.ALUNO, turmaId)));
        }
    }

    private static class ResultadoAcumulado {
        private final Set<GrupoWhatsapp> grupos = new HashSet<>();
        private final List<DestinoSemGrupo> semGrupo = new ArrayList<>();

        Resultado resultado() {
            return new Resultado(grupos, semGrupo);
        }
    }
}
