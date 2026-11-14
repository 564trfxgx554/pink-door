package com.pinkdoor.quiz.config;

import com.pinkdoor.config.properties.CorsProperties;
import com.pinkdoor.quiz.handler.QuizWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 注册默契问答的 WebSocket 端点。
 * 允许的来源与 REST 的 CORS 白名单保持一致。
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class QuizWebSocketConfig implements WebSocketConfigurer {

    private final QuizHandshakeInterceptor quizHandshakeInterceptor;
    private final QuizWebSocketHandler quizWebSocketHandler;
    private final CorsProperties corsProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(quizWebSocketHandler, "/ws/quiz")
                .addInterceptors(quizHandshakeInterceptor)
                .setAllowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new));
    }
}
