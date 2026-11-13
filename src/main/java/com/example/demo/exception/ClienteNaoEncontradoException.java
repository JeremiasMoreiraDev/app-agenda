package com.example.demo.exception;

public class ClienteNaoEncontradoException extends RuntimeException {

    public ClienteNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}