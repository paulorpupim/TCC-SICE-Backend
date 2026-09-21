package com.tccds.sice.modules.curso;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tccds.sice.exception.EntidadeNaoEncontradaException;
import com.tccds.sice.modules.curso.dto.CriarCursoDTO;
import com.tccds.sice.modules.curso.dto.CursoResponseDTO;
import com.tccds.sice.modules.curso.dto.EditarCursoDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    public Curso buscarCursoId(Long id) {
        return cursoRepository.findById(id).orElseThrow(
                () -> new EntidadeNaoEncontradaException("Curso não encontrado"));
    }

    public CursoResponseDTO buscarCursoPorId(Long id) {
        Curso curso = buscarCursoId(id);

        return new CursoResponseDTO(curso);
    }

    public CursoResponseDTO criar(CriarCursoDTO dto) {

        Curso curso = new Curso(
                dto.nome());

        Curso cursoSalvo = cursoRepository.save(curso);

        return new CursoResponseDTO(cursoSalvo);

    }

    public List<CursoResponseDTO> listarTodos() {

        return cursoRepository.findAll()
                .stream()
                .map(CursoResponseDTO::new)
                .toList();

    }

    public CursoResponseDTO editar(Long id, EditarCursoDTO dto) {
        Curso curso = buscarCursoId(id);

        curso.setNome(dto.nome());

        cursoRepository.save(curso);

        return new CursoResponseDTO(curso);
    }

    public void alterarStatus(Long id, boolean ativo) {

        Curso curso = buscarCursoId(id);
        curso.setAtivo(ativo);

        cursoRepository.save(curso);
    }

}
