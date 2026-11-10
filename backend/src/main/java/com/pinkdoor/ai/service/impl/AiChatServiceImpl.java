package com.pinkdoor.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinkdoor.ai.client.AiClient;
import com.pinkdoor.ai.config.AiProperties;
import com.pinkdoor.ai.dto.AiChatPayload;
import com.pinkdoor.ai.dto.AiChatRequest;
import com.pinkdoor.ai.entity.AiConversation;
import com.pinkdoor.ai.entity.AiMessage;
import com.pinkdoor.ai.service.AiChatService;
import com.pinkdoor.ai.service.AiConversationService;
import com.pinkdoor.ai.service.AiMessageService;
import com.pinkdoor.ai.vo.AiConversationVO;
import com.pinkdoor.ai.vo.AiMessageVO;
import com.pinkdoor.common.ResultCode;
import com.pinkdoor.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI 聊天实现：Java 负责鉴权、持久化和 SSE 转发，Python 只负责生成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final int HISTORY_LIMIT = 20;
    private static final int TITLE_MAX_LENGTH = 30;
    private static final long SSE_TIMEOUT_MILLIS = 120_000L;
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String ERROR_MESSAGE = "小助手暂时有点走神，等会儿再试试好吗？";

    private final AiConversationService conversationService;
    private final AiMessageService messageService;
    private final AiClient aiClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public List<AiConversationVO> listConversations(Long userId) {
        return conversationService.lambdaQuery()
                .eq(AiConversation::getUserId, userId)
                .orderByDesc(AiConversation::getLastMessageAt)
                .list()
                .stream()
                .map(conversation -> AiConversationVO.builder()
                        .id(conversation.getId())
                        .title(conversation.getTitle())
                        .lastMessageAt(conversation.getLastMessageAt())
                        .build())
                .toList();
    }

    @Override
    public List<AiMessageVO> listMessages(Long userId, Long conversationId) {
        requireOwnedConversation(userId, conversationId);
        return messageService.lambdaQuery()
                .eq(AiMessage::getConversationId, conversationId)
                .orderByAsc(AiMessage::getId)
                .list()
                .stream()
                .map(message -> AiMessageVO.builder()
                        .id(message.getId())
                        .role(message.getRole())
                        .content(message.getContent())
                        .createdAt(message.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public SseEmitter streamChat(Long userId, String nickname, AiChatRequest request) {
        String content = request.getContent().trim();
        AiConversation conversation = resolveConversation(userId, request);
        saveMessage(conversation.getId(), ROLE_USER, content);
        touchConversation(conversation.getId());

        List<AiMessage> history = loadRecentMessages(conversation.getId());
        AiChatPayload payload = new AiChatPayload(
                userId,
                nickname,
                history.stream()
                        .map(message -> new AiChatPayload.ChatMessage(message.getRole(), message.getContent()))
                        .toList(),
                Map.of()
        );

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicReference<Disposable> subscription = new AtomicReference<>();
        StringBuilder answer = new StringBuilder();

        emitter.onCompletion(() -> dispose(subscription));
        emitter.onTimeout(() -> {
            dispose(subscription);
            emitter.complete();
        });

        sendEvent(emitter, Map.of(
                "type", "meta",
                "conversationId", conversation.getId(),
                "title", conversation.getTitle()
        ));

        Disposable disposable = aiClient.streamChat(payload)
                .timeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                .subscribe(
                        event -> handleEvent(event, emitter, answer, failed),
                        error -> handleError(error, emitter, conversation.getId(), answer, completed, subscription),
                        () -> handleComplete(emitter, conversation.getId(), answer, failed, completed, subscription)
                );
        subscription.set(disposable);
        return emitter;
    }

    private AiConversation resolveConversation(Long userId, AiChatRequest request) {
        if (request.getConversationId() == null) {
            AiConversation conversation = new AiConversation();
            conversation.setUserId(userId);
            conversation.setTitle(buildTitle(request.getContent()));
            if (!conversationService.save(conversation)) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "会话创建失败。");
            }
            return conversation;
        }
        return requireOwnedConversation(userId, request.getConversationId());
    }

    private AiConversation requireOwnedConversation(Long userId, Long conversationId) {
        AiConversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !userId.equals(conversation.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "会话不存在。");
        }
        return conversation;
    }

    private String buildTitle(String content) {
        String title = content.trim();
        return title.length() <= TITLE_MAX_LENGTH
                ? title
                : title.substring(0, TITLE_MAX_LENGTH) + "…";
    }

    private void saveMessage(Long conversationId, String role, String content) {
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        messageService.save(message);
    }

    private void touchConversation(Long conversationId) {
        conversationService.lambdaUpdate()
                .eq(AiConversation::getId, conversationId)
                .set(AiConversation::getLastMessageAt, LocalDateTime.now())
                .update();
    }

    private List<AiMessage> loadRecentMessages(Long conversationId) {
        Page<AiMessage> page = new Page<>(1, HISTORY_LIMIT, false);
        messageService.lambdaQuery()
                .eq(AiMessage::getConversationId, conversationId)
                .orderByDesc(AiMessage::getId)
                .page(page);
        List<AiMessage> records = new ArrayList<>(page.getRecords());
        Collections.reverse(records);
        return records;
    }

    private void handleEvent(ServerSentEvent<String> event, SseEmitter emitter,
                             StringBuilder answer, AtomicBoolean failed) {
        String data = event.data();
        if (!StringUtils.hasText(data)) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(data);
            String type = node.path("type").asText();
            if ("delta".equals(type)) {
                String delta = node.path("content").asText("");
                if (StringUtils.hasText(delta)) {
                    answer.append(delta);
                    sendEvent(emitter, Map.of("type", "delta", "content", delta));
                }
            } else if ("error".equals(type)) {
                failed.set(true);
                sendEvent(emitter, Map.of(
                        "type", "error",
                        "message", node.path("message").asText(ERROR_MESSAGE)
                ));
            }
            // Python 的 done 事件不直接转发：Java 落库后再发送自己的 done。
        } catch (JsonProcessingException ex) {
            log.warn("无法解析 AI 事件: {}", data, ex);
        }
    }

    private void handleError(Throwable error, SseEmitter emitter, Long conversationId,
                             StringBuilder answer, AtomicBoolean completed,
                             AtomicReference<Disposable> subscription) {
        log.error("调用 Python AI 服务失败", error);
        if (!completed.compareAndSet(false, true)) {
            return;
        }
        saveAssistant(conversationId, answer);
        sendEvent(emitter, Map.of("type", "error", "message", ERROR_MESSAGE));
        emitter.complete();
        dispose(subscription);
    }

    private void handleComplete(SseEmitter emitter, Long conversationId, StringBuilder answer,
                                AtomicBoolean failed, AtomicBoolean completed,
                                AtomicReference<Disposable> subscription) {
        if (!completed.compareAndSet(false, true)) {
            return;
        }
        Long messageId = saveAssistant(conversationId, answer);
        if (!failed.get()) {
            Map<String, Object> done = new LinkedHashMap<>();
            done.put("type", "done");
            if (messageId != null) {
                done.put("messageId", messageId);
            }
            sendEvent(emitter, done);
        }
        emitter.complete();
        dispose(subscription);
    }

    private Long saveAssistant(Long conversationId, StringBuilder answer) {
        String content = answer.toString().trim();
        if (!StringUtils.hasText(content)) {
            return null;
        }
        try {
            AiMessage message = new AiMessage();
            message.setConversationId(conversationId);
            message.setRole(ROLE_ASSISTANT);
            message.setContent(content);
            messageService.save(message);
            touchConversation(conversationId);
            return message.getId();
        } catch (Exception ex) {
            log.error("保存 AI 回复失败", ex);
            return null;
        }
    }

    private void sendEvent(SseEmitter emitter, Map<String, ?> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            emitter.send(SseEmitter.event().data(json));
        } catch (Exception ex) {
            log.debug("SSE 发送失败，客户端可能已断开: {}", ex.getMessage());
        }
    }

    private void dispose(AtomicReference<Disposable> subscription) {
        Disposable disposable = subscription.get();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
