package com.ecommerce.project.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;


import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ★ OPTIONS 허용
                        .requestMatchers(HttpMethod.PATCH, "/api/address/**").permitAll() // ★ PATCH 허용

                        .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/me").permitAll()

                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

//        // ❌ addAllowedOriginPattern("*") → credentials=true 와 충돌
//        // ⭕ 정확한 도메인 명시
//        config.setAllowedOrigins(List.of("http://localhost:3000"));  // 로컬 개발용
//        config.addAllowedOriginPattern("https://frontend-green-one-32.vercel.app");  // 커스텀 도메인 연결용
//        config.addAllowedOriginPattern("https://frontend-bes01bkz1-limyuhaas-projects.vercel.app");  // Vercel 배포 프론트 허용
//
//        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
//
//        config.addAllowedHeader("*");
//        config.setAllowCredentials(true);

        config.setAllowCredentials(true);

        // 🔥 로컬 개발 환경 + 배포 환경 모두 패턴 기반으로 허용
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("https://*.vercel.app");

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
