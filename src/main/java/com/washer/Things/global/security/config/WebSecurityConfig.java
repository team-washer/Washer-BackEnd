package com.washer.Things.global.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.washer.Things.global.filter.JwtExceptionFilter;
import com.washer.Things.global.filter.JwtFilter;
import com.washer.Things.global.exception.handler.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtFilter jwtFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers(HttpMethod.POST, "/fcm-token/test").hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/machine/admin/reports").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/machine/admin/reports/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/machine/admin/out-of-order").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/machine/admin/out-of-order").hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/reservation/admin/reservations").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/reservation/admin/**").hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/user/admin/user/info").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/user/admin/*/restrict").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/user/admin/*/unrestrict").hasRole("ADMIN")
                                .anyRequest().permitAll()
                )
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtFilter.class)
                .exceptionHandling(handlingConfigurer ->
                        handlingConfigurer.accessDeniedHandler(new CustomAccessDeniedHandler(objectMapper))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(false);
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setMaxAge(86400L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}