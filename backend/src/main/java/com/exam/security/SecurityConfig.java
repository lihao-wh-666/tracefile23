package com.exam.security;

import com.exam.service.SystemConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
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

    private final JwtUtils jwtUtils;

    public SecurityConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtUtils);
    }

    @Bean
    public SessionTimeoutFilter sessionTimeoutFilter(RedisTemplate<String, Object> redisTemplate,
                                                      SystemConfigService systemConfigService,
                                                      ObjectMapper objectMapper,
                                                      JwtUtils jwtUtils) {
        return new SessionTimeoutFilter(redisTemplate, systemConfigService, objectMapper, jwtUtils);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                            JwtFilter jwtFilter,
                                            SessionTimeoutFilter sessionTimeoutFilter) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/api/system-config/value/**").permitAll()
                .antMatchers("/system-config/value/**").permitAll()
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
        http.addFilterBefore(sessionTimeoutFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
