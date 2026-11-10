package com.pinkdoor.ai.client;

import com.pinkdoor.ai.config.AiProperties;
import com.pinkdoor.ai.dto.AiChatPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * Python AI 服务的内部客户端，只被 Java 调用。
 */
@Component
@RequiredArgsConstructor
public class AiClient {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final WebClient aiWebClient;
    private final AiProperties aiProperties;

    /**
     * 调用 Python 的流式聊天接口，返回 SSE 事件流。
     */
    public Flux<ServerSentEvent<String>> streamChat(AiChatPayload payload) {
        return aiWebClient.post()
                .uri("/internal/chat/stream")
                .header(INTERNAL_TOKEN_HEADER, aiProperties.getInternalToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                });
    }
}
