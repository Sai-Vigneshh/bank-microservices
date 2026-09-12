package com.bank.gateway.validator;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    // ఎటువంటి టోకెన్ లేకుండా నేరుగా అనుమతించే పబ్లిక్ ఎండ్‌పాయింట్స్
    public static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/login",
            "/eureka",
            "/actuator/health"
    );

    // ఒక రిక్వెస్ట్ సెక్యూర్డ్ కాదా అని ధృవీకరించే ఫంక్షనల్ ప్రెడికేట్
    public Predicate<ServerHttpRequest> isSecured =
            request -> OPEN_API_ENDPOINTS
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}