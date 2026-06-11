package com.exam.security;

import com.exam.common.Constants;
import com.exam.common.ErrorCode;
import com.exam.common.Result;
import com.exam.service.SystemConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class SessionTimeoutFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            Long userId = jwtUtils.getUserIdFromToken(token);

            if (userId != null) {
                String sessionKey = Constants.SESSION_LAST_ACTIVITY_PREFIX + userId;
                String lastActivityStr = (String) redisTemplate.opsForValue().get(sessionKey);

                int timeoutMinutes = systemConfigService.getIntValueByKey(
                        Constants.CONFIG_LOGIN_TIMEOUT,
                        Constants.DEFAULT_LOGIN_TIMEOUT_MINUTES
                );

                long timeoutMillis = (long) timeoutMinutes * 60 * 1000;

                if (lastActivityStr != null) {
                    long lastActivity = Long.parseLong(lastActivityStr);
                    long currentTime = System.currentTimeMillis();

                    if (currentTime - lastActivity > timeoutMillis) {
                        redisTemplate.delete(sessionKey);
                        SecurityContextHolder.clearContext();
                        sendTimeoutResponse(response);
                        return;
                    }
                }

                redisTemplate.opsForValue().set(
                        sessionKey,
                        String.valueOf(System.currentTimeMillis()),
                        timeoutMinutes + 5,
                        TimeUnit.MINUTES
                );
            }
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
