package com.exam.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final SessionTimeoutFilter sessionTimeoutFilter;

    public SecurityConfig(JwtFilter jwtFilter, SessionTimeoutFilter sessionTimeoutFilter) {
        this.jwtFilter = jwtFilter;
        this.sessionTimeoutFilter = sessionTimeoutFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/api/doc.html/**").permitAll()
                .antMatchers("/doc.html/**").permitAll()
                .antMatchers("/api/webjars/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/api/v2/api-docs").permitAll()
                .antMatchers("/v2/api-docs").permitAll()
                .antMatchers("/api/swagger-resources/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/api/uploads/**").permitAll()
                .antMatchers("/uploads/**").permitAll()
                .anyRequest().authenticated();

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(sessionTimeoutFilter, JwtFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
