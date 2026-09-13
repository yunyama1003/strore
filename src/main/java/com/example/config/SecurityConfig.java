package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        // Home use without login; retain CSRF protection on all writes.
        return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .build();
    }
}
