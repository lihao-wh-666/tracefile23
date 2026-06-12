package com.exam.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DebugFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DebugFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("[DebugFilter] Before FilterSecurityInterceptor - URI: {}", uri);
        log.info("[DebugFilter] Authentication: {}", authentication);
        if (authentication != null) {
            log.info("[DebugFilter]   - isAuthenticated: {}", authentication.isAuthenticated());
            log.info("[DebugFilter]   - principal: {}", authentication.getPrincipal());
            log.info("[DebugFilter]   - authorities: {}", authentication.getAuthorities());
            log.info("[DebugFilter]   - name: {}", authentication.getName());
        }
        filterChain.doFilter(request, response);
        log.info("[DebugFilter] After FilterSecurityInterceptor - Response status: {}", response.getStatus());
    }
}
