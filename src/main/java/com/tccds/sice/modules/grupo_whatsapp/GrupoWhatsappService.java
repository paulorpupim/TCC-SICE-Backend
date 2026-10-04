package com.tccds.sice.modules.grupo_whatsapp;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tccds.sice.enums.TipoGrupo;
import com.tccds.sice.modules.grupo_whatsapp.dto.CriarGrupoWhatsappDTO;
import com.tccds.sice.modules.grupo_whatsapp.dto.EditarGrupoWhatsappDTO;
import com.tccds.sice.modules.grupo_whatsapp.dto.GrupoWhatsappResponseDTO;
import com.tccds.sice.modules.turma.Turma;
import com.tccds.sice.modules.turma.TurmaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GrupoWhatsappService {

    private final GrupoWhatsappRepository grupoRepository;
    private final TurmaRepository turmaRepository;

    @Transactional(readOnly = true)
    public GrupoWhatsapp buscarGrupoId(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Grupo não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<GrupoWhatsappResponseDTO> listarTodos() {
        return grupoRepository.findAll()
                .stream()
                .map(GrupoWhatsappResponseDTO::new)
                .toList();
    }

    @Transactional
    public GrupoWhatsappResponseDTO criar(
            CriarGrupoWhatsappDTO dto) {

        if (grupoRepository.existsByIdentificador(
                dto.identificador())) {

            throw new RuntimeException(
                    "Já existe um grupo com esse identificador.");
        }

        Turma turma = validarETrazerTurma(
                dto.tipo(),
                dto.turmaId());

        GrupoWhatsapp grupo = new GrupoWhatsapp(
                dto.nome(),
                dto.identificador(),
                dto.tipo(),
                turma);

        grupoRepository.save(grupo);

        return new GrupoWhatsappResponseDTO(grupo);
    }

    private Turma validarETrazerTurma(
            TipoGrupo tipo,
            Long turmaId) {

        if (tipo == TipoGrupo.TURMA) {

            if (turmaId == null) {
                throw new RuntimeException(
                        "Grupos do tipo TURMA devem possuir uma turma.");
            }

            if (grupoRepository.existsByTurmaId(turmaId)) {
                throw new RuntimeException(
                        "Essa turma já possui um grupo do WhatsApp.");
            }

            return turmaRepository.findById(turmaId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Turma não encontrada."));
        }

        if (turmaId != null) {
            throw new RuntimeException(
                    "Grupos gerais não podem possuir uma turma.");
        }

        if (grupoRepository.existsByTipo(tipo)) {
            throw new RuntimeException(
                    "Já existe um grupo geral desse tipo.");
        }

        return null;
    }

    @Transactional
    public GrupoWhatsappResponseDTO atualizar(
            Long id,
            EditarGrupoWhatsappDTO dto) {

        GrupoWhatsapp grupo = buscarGrupoId(id);

        if (grupoRepository.existsByIdentificadorAndIdNot(
                dto.identificador(),
                id)) {

            throw new RuntimeException(
                    "Já existe um grupo com esse identificador.");
        }

        Turma turma = resolverTurmaParaAtualizacao(
                grupo,
                dto.tipo(),
                dto.turmaId());

        grupo.setNome(dto.nome());
        grupo.setIdentificador(dto.identificador());
        grupo.setTipo(dto.tipo());
        grupo.setTurma(turma);

        return new GrupoWhatsappResponseDTO(grupo);
    }

    private Turma resolverTurmaParaAtualizacao(
            GrupoWhatsapp grupo,
            TipoGrupo tipo,
            Long turmaId) {

        if (tipo == TipoGrupo.TURMA) {

            if (turmaId == null) {
                throw new RuntimeException(
                        "Grupos do tipo TURMA devem possuir uma turma.");
            }

            if (grupoRepository.existsByTurmaIdAndIdNot(
                    turmaId,
                    grupo.getId())) {

                throw new RuntimeException(
                        "Essa turma já possui um grupo do WhatsApp.");
            }

            return turmaRepository.findById(turmaId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Turma não encontrada."));
        }

        if (turmaId != null) {
            throw new RuntimeException(
                    "Grupos gerais não podem possuir uma turma.");
        }

        if (grupoRepository.existsByTipoAndIdNot(
                tipo,
                grupo.getId())) {

            throw new RuntimeException(
                    "Já existe um grupo geral desse tipo.");
        }

        return null;
    }

    @Transactional
    public void alterarStatus(
            Long id,
            boolean ativo) {

        GrupoWhatsapp grupo = buscarGrupoId(id);

        grupo.setAtivo(ativo);
    }
}