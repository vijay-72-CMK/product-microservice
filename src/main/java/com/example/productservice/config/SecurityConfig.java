package com.example.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/products/remove-product/**").hasAuthority("Admin")
                        .requestMatchers("/api/category/add-category/**").hasAuthority("Admin")
                        .requestMatchers("/api/products/add-product/**").hasAuthority("Admin")
                        .requestMatchers("/api/products/search").permitAll()
                        .requestMatchers("/api/products/all").permitAll()
                        .requestMatchers("/api/category/all").permitAll()
                        .requestMatchers("/api/products/get-quantity/**").permitAll()
                        .requestMatchers("/api/products/{id}").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new RequestValidationFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

}
