package com.bank.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(RuntimeException.class)
public ResponseEntity <Map<String, Object>> handleRuntimeExcepmtion(RuntimeException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getMessage().contains("Access denied")) {
            status = HttpStatus.FORBIDDEN; // 403
        } else if (ex.getMessage().contains("not found")) {
            status = HttpStatus.NOT_FOUND; // 404
        }

        Map<String, Object> errorBody = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", ex.getMessage()
        );

        return ResponseEntity.status(status).body(errorBody);
}
}