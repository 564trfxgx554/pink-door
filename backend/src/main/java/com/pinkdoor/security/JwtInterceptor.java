package com.pinkdoor.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinkdoor.common.Result;
import com.pinkdoor.common.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 校验受保护接口的 {@code Authorization: Bearer <token>} 请求头。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CLAIM_NICKNAME = "nickname";
    private static final String CLAIM_COUPLE_ID = "couple_id";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, ResultCode.UNAUTHORIZED.getMessage());
            return false;
        }
        try {
            Claims claims = jwtTokenProvider.parseToken(header.substring(BEARER_PREFIX.length()));
            Long userId = Long.valueOf(claims.getSubject());
            String nickname = claims.get(CLAIM_NICKNAME, String.class);
            // 旧 token 没有 couple_id，直接拒绝，避免无租户上下文的数据访问。
            Object coupleIdClaim = claims.get(CLAIM_COUPLE_ID);
            if (coupleIdClaim == null) {
                log.warn("Rejected token without couple_id, subject={}", userId);
                writeUnauthorized(response, "Your login is outdated, please log in again.");
                return false;
            }
            Long coupleId = Long.valueOf(coupleIdClaim.toString());
            UserContext.set(new LoginUser(userId, nickname, coupleId));
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Rejected token: {}", ex.getMessage());
            writeUnauthorized(response, "Your login has expired, please log in again.");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(),
                Result.failure(ResultCode.UNAUTHORIZED.getCode(), message));
    }
}
