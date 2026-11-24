package com.lexflow.api.exception;

import com.lexflow.app.exception.EscritorioAtualIndisponivelException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EscritorioAtualIndisponivelException.class)
    public ResponseEntity<Map<String, Object>> tratarEscritorioIndisponivel(
            EscritorioAtualIndisponivelException ex) {

        Map<String, Object> corpo = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Escritório indisponível",
                "message", ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(corpo);
    }
}