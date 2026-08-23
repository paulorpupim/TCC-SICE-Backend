package com.tccds.sice.modules.aluno.matricula;

import org.springframework.stereotype.Service;

import com.tccds.sice.modules.aluno.Aluno;
import com.tccds.sice.modules.turma.Turma;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatriculaService {
    
    private final MatriculaRepository matriculaRepository;

    public Matricula criar(Aluno aluno, Turma turma){

        Matricula matricula = new Matricula(aluno, turma, true);
        
        return matriculaRepository.save(matricula);

    }

}
