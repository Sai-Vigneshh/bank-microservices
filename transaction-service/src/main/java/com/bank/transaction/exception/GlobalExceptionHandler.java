package com.bank.transaction.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        String responseBody = ex.contentUTF8();

        // account-service నుండి వచ్చిన మెసేజ్‌ను బట్టి స్టేటస్ కోడ్ నిర్ణయించడం
        if (responseBody.contains("Access Denied")) {
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("error", "Forbidden");
            response.put("message", "Access Denied: You do not own this account");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (responseBody.contains("Insufficient balance")) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Bad Request");
            response.put("message", "Insufficient balance");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("status", ex.status() > 0 ? ex.status() : HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Downstream Service Error");
        response.put("message", responseBody.isEmpty() ? ex.getMessage() : responseBody);

        return ResponseEntity.status(response.get("status") instanceof Integer ? (Integer) response.get("status") : 500).body(response);
    }
}