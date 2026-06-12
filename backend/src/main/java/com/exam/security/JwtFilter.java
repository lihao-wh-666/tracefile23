package com.exam.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtils jwtUtils;

    public JwtFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String token = resolveToken(request);
        log.info("[JwtFilter] URI: {}, hasToken: {}", uri, StringUtils.hasText(token));

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            Long userId = jwtUtils.getUserIdFromToken(token);
            Integer role = jwtUtils.getRoleFromToken(token);
            log.info("[JwtFilter] Token valid - userId: {}, role: {}", userId, role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("[JwtFilter] Authentication set: isAuthenticated={}, authorities={}",
                    authentication.isAuthenticated(), authentication.getAuthorities());
        } else if (StringUtils.hasText(token)) {
            log.warn("[JwtFilter] Token present but INVALID for URI: {}", uri);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/doc.html")
                || path.startsWith("/api/swagger-resources/")
                || path.startsWith("/api/webjars/")
                || path.startsWith("/api/v2/api-docs");
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
