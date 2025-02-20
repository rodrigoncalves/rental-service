package com.code.rental.config;

import com.code.rental.security.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public JwtProvider jwtProvider() {
        JwtProvider jwtProvider = new JwtProvider();
        jwtProvider.setJwtSecret(java.util.UUID.randomUUID().toString());
        return jwtProvider;
    }
}