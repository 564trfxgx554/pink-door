package com.pinkdoor.ai.controller;

import com.pinkdoor.ai.dto.AiChatRequest;
import com.pinkdoor.ai.service.AiChatService;
import com.pinkdoor.ai.vo.AiConversationVO;
import com.pinkdoor.ai.vo.AiMessageVO;
import com.pinkdoor.common.Result;
import com.pinkdoor.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 前端访问的 AI 接口。Python 服务只被 Java 调用，前端不直接访问。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @GetMapping("/conversations")
    public Result<List<AiConversationVO>> listConversations() {
        return Result.success(aiChatService.listConversations(UserContext.getUserId()));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public Result<List<AiMessageVO>> listMessages(@PathVariable Long conversationId) {
        return Result.success(aiChatService.listMessages(UserContext.getUserId(), conversationId));
    }

    /**
     * SSE 流式聊天。conversationId 为空时自动创建新会话。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody AiChatRequest request) {
        return aiChatService.streamChat(
                UserContext.getUserId(),
                UserContext.getNickname(),
                request
        );
    }
}
