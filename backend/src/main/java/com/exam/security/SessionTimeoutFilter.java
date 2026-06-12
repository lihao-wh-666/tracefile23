package com.exam.security;

import com.exam.common.Constants;
import com.exam.common.ErrorCode;
import com.exam.common.Result;
import com.exam.service.SystemConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class SessionTimeoutFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionTimeoutFilter.class);

    private final RedisTemplate<String, Object> redisTemplate;

    private final SystemConfigService systemConfigService;

    private final ObjectMapper objectMapper;

    private final JwtUtils jwtUtils;

    public SessionTimeoutFilter(RedisTemplate<String, Object> redisTemplate,
                                SystemConfigService systemConfigService,
                                ObjectMapper objectMapper,
                                JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.systemConfigService = systemConfigService;
        this.objectMapper = objectMapper;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String token = resolveToken(request);
        log.info("[SessionTimeoutFilter] START - URI: {}, hasToken: {}", uri, StringUtils.hasText(token));
        log.info("[SessionTimeoutFilter] At START - SecurityContext auth: {}",
                SecurityContextHolder.getContext().getAuthentication());

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            Long userId = jwtUtils.getUserIdFromToken(token);
            log.info("[SessionTimeoutFilter] Token valid, userId: {}", userId);

            if (userId != null) {
                String sessionKey = Constants.SESSION_LAST_ACTIVITY_PREFIX + userId;
                String lastActivityStr = (String) redisTemplate.opsForValue().get(sessionKey);
                log.info("[SessionTimeoutFilter] lastActivityStr from Redis: {}", lastActivityStr);

                int timeoutMinutes = systemConfigService.getIntValueByKey(
                        Constants.CONFIG_LOGIN_TIMEOUT,
                        Constants.DEFAULT_LOGIN_TIMEOUT_MINUTES
                );
                log.info("[SessionTimeoutFilter] timeoutMinutes configured: {}", timeoutMinutes);

                long timeoutMillis = (long) timeoutMinutes * 60 * 1000;

                if (lastActivityStr != null) {
                    long lastActivity = Long.parseLong(lastActivityStr);
                    long currentTime = System.currentTimeMillis();
                    long diff = currentTime - lastActivity;
                    log.info("[SessionTimeoutFilter] time diff: {}ms, timeoutMillis: {}ms, timeout: {}",
                            diff, timeoutMillis, diff > timeoutMillis);

                    if (currentTime - lastActivity > timeoutMillis) {
                        log.warn("[SessionTimeoutFilter] SESSION EXPIRED for userId: {}", userId);
                        redisTemplate.delete(sessionKey);
                        SecurityContextHolder.clearContext();
                        sendTimeoutResponse(response);
                        return;
                    }
                }

                String newActivityTime = String.valueOf(System.currentTimeMillis());
                redisTemplate.opsForValue().set(
                        sessionKey,
                        newActivityTime,
                        timeoutMinutes + 5,
                        TimeUnit.MINUTES
                );
                log.info("[SessionTimeoutFilter] Updated last-activity for userId: {} -> {}", userId, newActivityTime);
            }
        } else if (StringUtils.hasText(token)) {
            log.warn("[SessionTimeoutFilter] Token present but INVALID for URI: {}", uri);
        }

        log.info("[SessionTimeoutFilter] Continuing filter chain for URI: {}", uri);
        filterChain.doFilter(request, response);
        log.info("[SessionTimeoutFilter] After filter chain - SecurityContext auth: {}",
                SecurityContextHolder.getContext().getAuthentication());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/doc.html")
                || path.startsWith("/api/swagger-resources/")
                || path.startsWith("/api/webjars/")
                || path.startsWith("/api/v2/api-docs")
                || path.startsWith("/api/uploads/");
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendTimeoutResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.fail(ErrorCode.TOKEN_EXPIRED.getCode(), "登录已超时，请重新登录");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
