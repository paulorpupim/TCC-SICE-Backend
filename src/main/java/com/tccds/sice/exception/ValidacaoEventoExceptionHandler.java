package com.tccds.sice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Erros de validação de regras de negócio (ex.: reativar evento passado). */
@RestControllerAdvice
public class ValidacaoEventoExceptionHandler {
    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> entidadeNaoEncontrada(EntidadeNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(RegraNegocioCadastroException.class)
    public ResponseEntity<Map<String, String>> regraDeCadastro(RegraNegocioCadastroException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(RegraNegocioEventoException.class)
    public ResponseEntity<Map<String, String>> regraDeNegocio(RegraNegocioEventoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensagem", ex.getMessage()));
    }
}
