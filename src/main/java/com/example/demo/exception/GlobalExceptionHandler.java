package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> clienteNaoEncontrado(ClienteNaoEncontradoException ex) {
        Map<String, String> corpo = new HashMap<>();
        corpo.put("erro", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> dadosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("erro", "Dados inválidos");
        corpo.put("campos", campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }
}