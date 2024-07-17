package com.gettimhired.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    @Profile("!local")
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .requiresChannel(channel ->
                        channel.anyRequest().requiresSecure())
                .securityMatchers(matchers -> {
                    matchers.requestMatchers("/api/**");
                })
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(basic -> basic.init(http))
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/api/users/*/id").hasRole("SYSTEM");
                    authorize.requestMatchers("/api/users/*/email").hasRole("SYSTEM");
                    authorize.requestMatchers("/api/users/*/password").hasRole("USER");
                    authorize.requestMatchers(HttpMethod.POST, "/api/users").permitAll();
                    authorize.anyRequest().authenticated();
                })
                .userDetailsService(customUserDetailsService)
                .build();
    }

    @Bean
    @Order(0)
    @Profile("local")
    SecurityFilterChain filterChainLocal(HttpSecurity http) throws Exception {
        return http
                .securityMatchers(matchers -> {
                    matchers.requestMatchers("/api/**");
                })
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(basic -> basic.init(http))
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/api/users/*/id").hasRole("SYSTEM");
                    authorize.requestMatchers("/api/users/*/email").hasRole("SYSTEM");
                    authorize.requestMatchers("/api/users/*/password").hasRole("USER");
                    authorize.requestMatchers(HttpMethod.POST, "/api/users").permitAll();
                    authorize.anyRequest().authenticated();
                })
                .userDetailsService(customUserDetailsService)
                .build();
    }
}
