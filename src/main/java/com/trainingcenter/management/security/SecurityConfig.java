/**
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
    /**@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure authentication provider to use custom UserDetailsService
     */
   /** @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Configure authentication manager
     */
   /** @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(authenticationProvider());
        return authBuilder.build();
    }

    /**
     * Configure HTTP security filter chain
     * - Keep frontend integration simple: most endpoints are public
     * - Restrict only important management endpoints to ADMIN
     * - Disable Spring Security default form login page
     */
    /**@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Authorize requests
                .authorizeHttpRequests(auth -> auth
                // Public endpoints for auth/health/docs/webhooks
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/otp/send", "/api/otp/verify").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/webhook/**", "/api/webhooks/**").permitAll()

                // ADMIN-only on users and important tables
                .requestMatchers(HttpMethod.POST, "/api/users/**", "/api/tenants/**", "/api/registers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**", "/api/tenants/**", "/api/registers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/users/**", "/api/tenants/**", "/api/registers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**", "/api/tenants/**", "/api/registers/**").hasRole("ADMIN")

                // Payment initiation requires logged-in user
                .requestMatchers("/api/payments/**").authenticated()

                // Keep the rest open for easy frontend integration
                .anyRequest().permitAll()
                )
                // Use HTTP Basic authentication
                .httpBasic(basic -> {})
                // Disable default Spring Security generated login page
                .formLogin(form -> form.disable())
                // Disable CSRF for simplicity (enable later for form-based apps)
                .csrf(csrf -> csrf.disable())
                // Use stateless session for API (JWT-ready)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
**/

package com.trainingcenter.management.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}