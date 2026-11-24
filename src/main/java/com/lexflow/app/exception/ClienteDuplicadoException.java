package com.lexflow.app.exception;

public class ClienteDuplicadoException extends RuntimeException {

    public ClienteDuplicadoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}