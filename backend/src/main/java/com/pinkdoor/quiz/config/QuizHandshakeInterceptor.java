package com.pinkdoor.quiz.config;

import com.pinkdoor.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * WebSocket 握手拦截。
 * 浏览器不能带 Authorization 请求头，所以 token 走查询参数 ?token=xxx，
 * 验过后把 userId + coupleId 存进连接，后续消息直接从这里取。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizHandshakeInterceptor implements HandshakeInterceptor {

    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_NICKNAME = "nickname";
    private static final String ATTR_COUPLE_ID = "coupleId";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");
        if (!StringUtils.hasText(token)) {
            log.warn("Quiz handshake rejected: missing token");
            return false;
        }
        try {
            Claims claims = jwtTokenProvider.parseToken(token);
            Object coupleIdClaim = claims.get("couple_id");
            if (coupleIdClaim == null) {
                log.warn("Quiz handshake rejected: token without couple_id");
                return false;
            }
            attributes.put(ATTR_USER_ID, Long.valueOf(claims.getSubject()));
            attributes.put(ATTR_NICKNAME, claims.get("nickname", String.class));
            attributes.put(ATTR_COUPLE_ID, Long.valueOf(coupleIdClaim.toString()));
            return true;
        } catch (Exception ex) {
            log.warn("Quiz handshake rejected: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后不需要额外处理。
    }
}
