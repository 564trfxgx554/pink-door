package com.pinkdoor.ai.service;

import com.pinkdoor.ai.dto.AiChatRequest;
import com.pinkdoor.ai.vo.AiConversationVO;
import com.pinkdoor.ai.vo.AiMessageVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 聊天门面：会话、历史与流式对话。
 */
public interface AiChatService {

    /**
     * 查询当前用户的会话列表。
     */
    List<AiConversationVO> listConversations(Long userId);

    /**
     * 查询某个会话的全部消息，并校验归属。
     */
    List<AiMessageVO> listMessages(Long userId, Long conversationId);

    /**
     * 保存用户消息，调用 Python AI 服务并流式转发。
     */
    SseEmitter streamChat(Long userId, String nickname, AiChatRequest request);
}
