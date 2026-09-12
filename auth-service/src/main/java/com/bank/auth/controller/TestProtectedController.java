package com.bank.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class TestProtectedController {


    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String role,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access granted to protected endpoint!");
        response.put("forwardedUserId", userId);
        response.put("forwardedRole", role);
        response.put("forwardedEmail", email);

        return ResponseEntity.ok(response);
    }
}