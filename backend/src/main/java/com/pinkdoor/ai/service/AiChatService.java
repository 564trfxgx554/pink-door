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
     * 查询当前用户在所属空间内的会话列表。
     *
     * @param userId   当前用户
     * @param coupleId 当前情侣空间
     */
    List<AiConversationVO> listConversations(Long userId, Long coupleId);

    /**
     * 查询某个会话的全部消息，并校验空间和用户归属。
     */
    List<AiMessageVO> listMessages(Long userId, Long coupleId, Long conversationId);

    /**
     * 删除当前用户在自己空间内的会话及其全部消息。
     */
    void deleteConversation(Long userId, Long coupleId, Long conversationId);

    /**
     * 保存用户消息，调用 Python AI 服务并流式转发。
     */
    SseEmitter streamChat(Long userId, Long coupleId, String nickname, AiChatRequest request);
}
