package com.pinkdoor.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * 发给 Python AI 服务的请求体。Python 无状态，历史由 Java 提供。
 * 字段名使用 Python 侧的 snake_case 契约。
 */
public record AiChatPayload(
        @JsonProperty("user_id") Long userId,
        String nickname,
        List<ChatMessage> messages,
        Map<String, String> facts
) {

    /**
     * 单条历史消息。
     */
    public record ChatMessage(String role, String content) {
    }
}
