package com.bank.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    // క్లయింట్ యొక్క IP అడ్రస్‌ను బట్టి రేట్ లిమిట్ కీని డిసైడ్ చేసే బీన్
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                        .getAddress()
                        .getHostAddress()
        );
    }
}