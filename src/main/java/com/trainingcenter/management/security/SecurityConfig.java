package com.trainingcenter.management.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Configure password encoder (BCrypt for security)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure authentication provider to use custom UserDetailsService
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Configure authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(authenticationProvider());
        return authBuilder.build();
    }

    /**
     * Configure HTTP security filter chain
     * - Allow public GET access to read endpoints, registration, login, and health
     * - Require authentication for POST, PUT, DELETE endpoints
     * - Use HTTP Basic authentication for now (easily replaceable with JWT)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Authorize requests
                .authorizeHttpRequests(auth -> auth
                        // Authentication endpoints - POST allowed (for login)
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/**").permitAll()
                        // Health and Swagger - GET only
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Public GET endpoints - read-only (GET only)
                        .requestMatchers(HttpMethod.GET, "/api/registers/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/training-sessions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/institutes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/classrooms/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lectures/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/**").permitAll()

                        // Webhook - allow unauthenticated (verified by Stripe signature)
                        .requestMatchers("/webhook/**").permitAll()
                        .requestMatchers("/api/webhooks/**").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                // Use HTTP Basic authentication
                .httpBasic(basic -> {})
                // Disable CSRF for simplicity (enable later for form-based apps)
                .csrf(csrf -> csrf.disable())
                // Use stateless session for API (JWT-ready)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}


