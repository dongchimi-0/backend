package com.ecommerce.project.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())   // CSRF 비활성화
                .cors(cors -> cors.disable())   // 필요하면 CORS 켜도 됨
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/products").permitAll()  // <-- 여기!
                        .anyRequest().permitAll()
                );

        return http.build();
    }


    @Configuration
    public class AppConfig {
        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

}
