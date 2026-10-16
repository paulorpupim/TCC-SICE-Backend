package com.tccds.sice.modules.envio_aviso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tccds.sice.enums.Etapa;
import com.tccds.sice.enums.PerfilUsuario;
import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.evento.Evento;
import com.tccds.sice.modules.evento.evento_destino.EventoDestino;
import com.tccds.sice.modules.evento.evento_destino_turma.dto.CriarEventoDestinoDTO;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsapp;
import com.tccds.sice.modules.grupo_whatsapp.GrupoWhatsappRepository;

@ExtendWith(MockitoExtension.class)
class ResolverGruposAvisoServiceTest {

    @Mock GrupoWhatsappRepository repository;
    @InjectMocks ResolverGruposAvisoService service;

    @Test
    void avisaQuandoNaoExisteGrupoGeralProfessores() {
        when(repository.findByTipoAndAtivoTrue(TipoGrupo.GERAL_PROFESSORES))
            .thenReturn(Optional.empty());
        var resultado = service.resolver(Set.of(
            new CriarEventoDestinoDTO(PerfilUsuario.PROFESSOR, Set.of(), Set.of(), Set.of())));
        assertTrue(resultado.grupos().isEmpty());
        assertEquals(1, resultado.semGrupo().size());
        assertEquals(PerfilUsuario.PROFESSOR, resultado.semGrupo().get(0).perfil());
    }

    @Test
    void verificaGrupoGeralAlunosMesmoComFiltroDeEtapaSemTurma() {
        var grupo = new GrupoWhatsapp("Alunos", "grupo@g.us", TipoGrupo.GERAL_ALUNOS, null);
        when(repository.findByTipoAndAtivoTrue(TipoGrupo.GERAL_ALUNOS))
            .thenReturn(Optional.of(grupo));
        var resultado = service.resolver(Set.of(
            new CriarEventoDestinoDTO(PerfilUsuario.ALUNO,
                Set.of(Etapa.PRIMEIRA), Set.of(), Set.of())));
        assertEquals(1, resultado.grupos().size());
        assertTrue(resultado.semGrupo().isEmpty());
        verify(repository).findByTipoAndAtivoTrue(TipoGrupo.GERAL_ALUNOS);
    }

    @Test
    void avisaParcialmenteQuandoApenasUmaTurmaTemGrupoAtivo() {
        var grupo = new GrupoWhatsapp("Turma 10", "turma@g.us", TipoGrupo.TURMA, null);
        when(repository.findByTurmaIdAndAtivoTrue(10L)).thenReturn(Optional.of(grupo));
        when(repository.findByTurmaIdAndAtivoTrue(20L)).thenReturn(Optional.empty());
        var resultado = service.resolver(Set.of(
            new CriarEventoDestinoDTO(PerfilUsuario.ALUNO, Set.of(), Set.of(), Set.of(10L, 20L))));
        assertEquals(1, resultado.grupos().size());
        assertEquals(1, resultado.semGrupo().size());
        assertEquals(20L, resultado.semGrupo().get(0).turmaId());
    }

    @Test
    void eventoPersistidoUsaMesmaRegraDaPrevia() {
        var destino = new EventoDestino(PerfilUsuario.PROFESSOR, Set.of(), Set.of());
        var evento = new Evento();
        evento.adicionarDestino(destino);
        when(repository.findByTipoAndAtivoTrue(TipoGrupo.GERAL_PROFESSORES))
            .thenReturn(Optional.empty());
        var resultado = service.resolver(evento);
        assertEquals(1, resultado.semGrupo().size());
        assertTrue(resultado.grupos().isEmpty());
    }
}
