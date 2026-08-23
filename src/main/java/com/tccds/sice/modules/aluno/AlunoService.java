package com.tccds.sice.modules.aluno;

import org.springframework.stereotype.Service;

import com.tccds.sice.modules.usuario.Usuario;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlunoService {
    
    private final AlunoRepository alunoRepository;

    public Aluno criar(Usuario usuario){

        Aluno aluno = new Aluno(usuario);

        return alunoRepository.save(aluno);

    }

}
