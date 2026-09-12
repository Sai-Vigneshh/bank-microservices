package com.bank.gateway.filter;

import com.bank.gateway.util.JwtUtil;
import com.bank.gateway.validator.RouterValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouterValidator routerValidator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
        // Properties if needed in future
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 1. రూట్ సెక్యూర్డ్ కాదా అని చెక్ చేయడం
            if (routerValidator.isSecured.test(request)) {

                // 2. Authorization Header ఉందో లేదో చూడటం
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.error("Missing Authorization Header for secure path: {}", request.getPath());
                    return onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.error("Invalid Authorization Header format: {}", request.getPath());
                    return onError(exchange, "Invalid Token structure. Must start with Bearer", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);

                try {
                    // 3. టోకెన్ సిగ్నేచర్ & ఎక్స్‌పైరీ వాలిడేషన్
                    jwtUtil.validateToken(token);

                    if (jwtUtil.isTokenExpired(token)) {
                        log.warn("Token expired for request: {}", request.getPath());
                        return onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
                    }

                    // 4. క్లెయిమ్స్ సేకరించడం
                    String userId = jwtUtil.extractUserId(token);
                    String role = jwtUtil.extractRole(token);
                    String email = jwtUtil.extractEmail(token);

                    // 5. హెడర్ మ్యుటేషన్: డౌన్‌స్ట్రీమ్ సర్వీసెస్‌కు అవసరమైన యూజర్ సమాచారాన్ని జోడించడం
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", userId != null ? userId : "")
                            .header("X-User-Roles", role != null ? role : "")
                            .header("X-User-Email", email != null ? email : "")
                            .build();

                    // 6. టెర్మినల్ కన్సోల్ లాగింగ్
                    log.info("=================================================");
                    log.info(">>> API Gateway Auth Filter: Token Validated");
                    log.info(">>> Path: {}", request.getPath());
                    log.info(">>> Forwarding X-User-Id: {}", userId);
                    log.info(">>> Forwarding X-User-Roles: {}", role);
                    log.info(">>> Forwarding X-User-Email: {}", email);
                    log.info("=================================================");

                    // 7. మ్యుటేట్ చేసిన రిక్వెస్ట్‌ను తర్వాతి ఫిల్టర్‌కి ఫార్వర్డ్ చేయడం
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());

                } catch (Exception ex) {
                    log.error("JWT Verification failed: {}", ex.getMessage());
                    return onError(exchange, "Unauthorized access: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
                }
            }

            // ఓపెన్/పబ్లిక్ రూట్ అయితే ఎలాంటి అడ్డంకులు లేకుండా నేరుగా పంపేయడం
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.warn("Gateway rejected request. Status: {}, Reason: {}", httpStatus, err);
        return response.setComplete();
    }
}