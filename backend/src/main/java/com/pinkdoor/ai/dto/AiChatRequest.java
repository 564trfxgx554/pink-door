package com.pinkdoor.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前端发起的 AI 聊天请求。
 */
@Data
public class AiChatRequest {

    /**
     * 为空时由后端自动创建新会话。
     */
    private Long conversationId;

    @NotBlank(message = "想聊点什么呀？")
    @Size(max = 2000, message = "一次说太多了，分开慢慢告诉我吧。")
    private String content;
}
