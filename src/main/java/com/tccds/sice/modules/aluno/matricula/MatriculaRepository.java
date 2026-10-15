package com.tccds.sice.modules.aluno.matricula;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    @Query("select m from Matricula m join fetch m.turma t join fetch t.curso "
            + "where m.aluno.id = :alunoId order by m.id desc")
    List<Matricula> listarPorAluno(@Param("alunoId") Long alunoId);

    @Query("select m from Matricula m join fetch m.aluno a "
            + "join fetch a.usuario u join fetch u.credencial "
            + "where m.turma.id = :turmaId order by u.nome asc, m.id asc")
    List<Matricula> listarPorTurma(@Param("turmaId") Long turmaId);

    boolean existsByAluno_IdAndTurma_Id(Long alunoId, Long turmaId);

}
