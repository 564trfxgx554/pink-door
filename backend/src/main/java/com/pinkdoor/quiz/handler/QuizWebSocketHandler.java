package com.pinkdoor.quiz.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinkdoor.quiz.manager.QuizRoomManager;
import com.pinkdoor.security.LoginUser;
import com.pinkdoor.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 默契问答 WebSocket 消息入口：只负责解析和分发，业务状态放在 QuizRoomManager。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizWebSocketHandler extends TextWebSocketHandler {

    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_NICKNAME = "nickname";
    private static final String ATTR_COUPLE_ID = "coupleId";

    private final QuizRoomManager roomManager;
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = attr(session, ATTR_USER_ID);
        Long coupleId = attr(session, ATTR_COUPLE_ID);
        String nickname = attr(session, ATTR_NICKNAME);
        if (userId == null || coupleId == null) {
            return;
        }

        // WebSocket 线程没有请求线程的 UserContext，手动设置给租户插件用。
        UserContext.set(new LoginUser(userId, nickname, coupleId));
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            switch (node.path("type").asText()) {
                case "join" -> roomManager.join(session, node.path("roomId").asLong());
                case "ready" -> roomManager.ready(session);
                case "answer" -> roomManager.answer(
                        session, node.path("roundNo").asInt(), node.path("optionIndex").asInt());
                default -> roomManager.sendError(session, "BAD_MESSAGE", "不认识的消息类型。");
            }
        } catch (Exception ex) {
            log.warn("处理 Quiz 消息失败", ex);
            roomManager.sendError(session, "BAD_MESSAGE", "消息格式不对。");
        } finally {
            UserContext.clear();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = attr(session, ATTR_USER_ID);
        Long coupleId = attr(session, ATTR_COUPLE_ID);
        if (userId == null || coupleId == null) {
            return;
        }
        UserContext.set(new LoginUser(userId, null, coupleId));
        try {
            roomManager.leave(session);
        } finally {
            UserContext.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T attr(WebSocketSession session, String name) {
        return (T) session.getAttributes().get(name);
    }
}
