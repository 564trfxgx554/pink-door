package com.pinkdoor.quiz.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinkdoor.quiz.entity.QuizQuestion;
import com.pinkdoor.quiz.entity.QuizRoom;
import com.pinkdoor.quiz.service.QuizService;
import com.pinkdoor.quiz.vo.QuizResultVO;
import com.pinkdoor.security.LoginUser;
import com.pinkdoor.security.UserContext;
import com.pinkdoor.user.entity.User;
import com.pinkdoor.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 默契问答房间的内存状态。
 * 单实例部署，房间只存在内存里；数据库负责落答案和结果。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizRoomManager {

    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_NICKNAME = "nickname";
    private static final String ATTR_COUPLE_ID = "coupleId";
    private static final String ATTR_ROOM_ID = "roomId";

    private static final int PLAYER_COUNT = 2;
    private static final int QUESTION_SECONDS = 15;
    private static final int REVEAL_SECONDS = 3;
    private static final int OFFLINE_SECONDS = 60;
    private static final String STATUS_WAITING = "waiting";
    private static final String STATUS_PLAYING = "playing";
    private static final String STATUS_FINISHED = "finished";
    private static final String ROUND_ANSWERING = "answering";
    private static final String ROUND_REVEALED = "revealed";

    private final QuizService quizService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    private final Map<Long, RoomState> rooms = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "quiz-timer");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * 加入房间：校验房间归属，注册连接，广播双方状态。
     */
    public void join(WebSocketSession session, Long roomId) {
        Long userId = attr(session, ATTR_USER_ID);
        Long coupleId = attr(session, ATTR_COUPLE_ID);

        QuizRoom room = quizService.getOwnedRoom(coupleId, roomId);
        if (room == null) {
            sendError(session, "ROOM_NOT_FOUND", "房间不存在。");
            return;
        }
        RoomState state = rooms.computeIfAbsent(roomId, id -> loadState(room));
        state.lock.lock();
        try {
            // 服务重启后内存里的题目已丢失，进行中的房间直接结算，避免前端一直等。
            if (STATUS_PLAYING.equals(state.status) && state.round == null) {
                interruptGame(state);
            }

            PlayerState player = state.players.get(userId);
            if (player == null) {
                sendError(session, "NOT_IN_ROOM", "你不是这个房间的成员。");
                return;
            }
            boolean wasOffline = !player.online;
            if (player.offlineFuture != null) {
                player.offlineFuture.cancel(false);
                player.offlineFuture = null;
            }
            // 同一个用户重连时，替换旧连接。
            player.session = new ConcurrentWebSocketSessionDecorator(session, 5000, 64 * 1024);
            player.sessionId = session.getId();
            player.online = true;
            session.getAttributes().put(ATTR_ROOM_ID, roomId);

            broadcast(state, buildJoinedPayload(state));
            // 重连时给本人补一份当前现场，前端据此恢复到答题 / 等待 / 揭晓页。
            send(player.session, buildSnapshot(state, userId));

            PlayerState partner = findPartner(state, userId);
            if (wasOffline && partner != null && partner.online) {
                send(partner.session, Map.of("type", "partnerOnline", "userId", userId));
            }
        } finally {
            state.lock.unlock();
        }
    }

    /**
     * 准备：双方都准备后自动开始，抽题并推第一题。
     */
    public void ready(WebSocketSession session) {
        RoomState state = findState(session);
        if (state == null) {
            sendError(session, "NOT_IN_ROOM", "你还没有加入房间。");
            return;
        }
        Long userId = attr(session, ATTR_USER_ID);
        state.lock.lock();
        try {
            PlayerState player = state.players.get(userId);
            if (player == null) {
                sendError(session, "NOT_IN_ROOM", "你不是这个房间的成员。");
                return;
            }
            player.ready = true;
            broadcast(state, buildReadyPayload(state));
            if (STATUS_WAITING.equals(state.status) && allReady(state)) {
                startGame(state);
            }
        } finally {
            state.lock.unlock();
        }
    }

    /**
     * 提交答案：写库、只回执、两人齐了立即揭晓。
     * 揭晓前绝不把选项发给对方，这是盲答的关键。
     */
    public void answer(WebSocketSession session, int roundNo, int optionIndex) {
        RoomState state = findState(session);
        if (state == null) {
            sendError(session, "NOT_IN_ROOM", "你还没有加入房间。");
            return;
        }
        Long userId = attr(session, ATTR_USER_ID);
        state.lock.lock();
        try {
            RoundState round = state.round;
            if (round == null || round.roundNo != roundNo || !ROUND_ANSWERING.equals(round.status)) {
                sendError(session, "ROUND_CLOSED", "这题已经结束了。");
                return;
            }
            if (System.currentTimeMillis() > round.deadlineAt) {
                sendError(session, "ROUND_CLOSED", "这题已经超时了。");
                return;
            }
            if (optionIndex < 0 || optionIndex >= round.options.size()) {
                sendError(session, "BAD_MESSAGE", "选项不存在。");
                return;
            }
            if (round.answers.containsKey(userId)) {
                sendError(session, "ALREADY_ANSWERED", "这题已经答过了。");
                return;
            }
            if (!quizService.saveAnswer(state.coupleId, state.roomId, roundNo, userId, optionIndex)) {
                sendError(session, "ALREADY_ANSWERED", "这题已经答过了。");
                return;
            }
            round.answers.put(userId, optionIndex);

            PlayerState me = state.players.get(userId);
            send(me == null ? session : me.session,
                    Map.of("type", "answerAccepted", "roundNo", roundNo));
            for (PlayerState player : state.players.values()) {
                if (!player.userId.equals(userId) && player.online) {
                    // 盲答：只告诉对方已提交，绝不带选项。
                    send(player.session, Map.of("type", "partnerAnswered", "roundNo", roundNo));
                }
            }

            if (round.answers.size() >= state.players.size()) {
                revealIfNeeded(state, roundNo);
            }
        } finally {
            state.lock.unlock();
        }
    }

    /**
     * 连接断开：标记离线，广播给对方。
     */
    public void leave(WebSocketSession session) {
        RoomState state = findState(session);
        if (state == null) {
            return;
        }
        Long userId = attr(session, ATTR_USER_ID);
        state.lock.lock();
        try {
            PlayerState player = state.players.get(userId);
            // 重连后旧连接关闭，不能把新连接标记成离线。
            if (player == null || !session.getId().equals(player.sessionId)) {
                return;
            }
            player.online = false;
            player.session = null;
            player.sessionId = null;
            broadcast(state, Map.of("type", "partnerOffline", "userId", userId));

            // 60 秒没回来就按已答的题结算。
            if (player.offlineFuture != null) {
                player.offlineFuture.cancel(false);
            }
            player.offlineFuture = scheduler.schedule(
                    () -> runWithTenant(state, () -> handleOfflineTimeout(state, userId)),
                    OFFLINE_SECONDS, TimeUnit.SECONDS);
        } finally {
            state.lock.unlock();
        }
    }

    /**
     * 掉线超过 60 秒：按已答的题结算。
     */
    private void handleOfflineTimeout(RoomState state, Long userId) {
        state.lock.lock();
        try {
            PlayerState player = state.players.get(userId);
            if (player == null || player.online || STATUS_FINISHED.equals(state.status)) {
                return;
            }
            log.info("Quiz room {} player {} offline timeout, finishing", state.roomId, userId);
            finishGame(state);
        } finally {
            state.lock.unlock();
        }
    }

    /**
     * 服务重启后内存状态丢失，把进行中的房间标记为结束，避免出现永远等不到下一题的房间。
     */
    private void interruptGame(RoomState state) {
        log.warn("Quiz room {} lost in-memory state, marking finished", state.roomId);
        state.status = STATUS_FINISHED;
        quizService.finishRoom(state.coupleId, state.roomId);
    }

    /**
     * 重连现场：只包含本人答案，未揭晓时绝不包含对方答案。
     */
    private Map<String, Object> buildSnapshot(RoomState state, Long userId) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("type", "snapshot");
        snapshot.put("state", resolveClientState(state, userId));
        snapshot.put("room", buildRoomInfo(state));

        RoundState round = state.round;
        if (round != null) {
            snapshot.put("roundNo", round.roundNo);
            Map<String, Object> question = new LinkedHashMap<>();
            question.put("content", round.question.getContent());
            question.put("options", round.options);
            snapshot.put("question", question);
            snapshot.put("startedAt", round.deadlineAt - QUESTION_SECONDS * 1000L);
            snapshot.put("deadlineAt", round.deadlineAt);
            snapshot.put("serverNow", System.currentTimeMillis());

            Integer myAnswer = round.answers.get(userId);
            Map<String, Object> me = new LinkedHashMap<>();
            me.put("userId", userId);
            me.put("answered", myAnswer != null);
            me.put("optionIndex", myAnswer == null ? -1 : myAnswer);
            snapshot.put("me", me);

            PlayerState partner = findPartner(state, userId);
            Map<String, Object> partnerInfo = new LinkedHashMap<>();
            if (partner != null) {
                partnerInfo.put("userId", partner.userId);
                partnerInfo.put("online", partner.online);
                partnerInfo.put("answered", round.answers.containsKey(partner.userId));
            }
            snapshot.put("partner", partnerInfo);

            if (ROUND_REVEALED.equals(round.status)) {
                Map<Long, Integer> answers = new LinkedHashMap<>();
                for (Long playerId : sortedPlayerIds(state)) {
                    answers.put(playerId, round.answers.get(playerId));
                }
                snapshot.put("reveal", buildRevealPayload(state, round, answers, allSame(answers)));
            }
        }

        if (STATUS_FINISHED.equals(state.status)) {
            QuizResultVO result = quizService.buildResult(state.coupleId, state.roomId);
            snapshot.put("result", objectMapper.convertValue(result,
                    new TypeReference<Map<String, Object>>() {
                    }));
        }
        return snapshot;
    }

    private String resolveClientState(RoomState state, Long userId) {
        if (STATUS_FINISHED.equals(state.status)) {
            return "finished";
        }
        if (STATUS_WAITING.equals(state.status)) {
            return "waiting";
        }
        RoundState round = state.round;
        if (round == null) {
            return "waiting";
        }
        if (ROUND_REVEALED.equals(round.status)) {
            return "reveal";
        }
        return round.answers.containsKey(userId) ? "waitingPartner" : "answering";
    }

    private PlayerState findPartner(RoomState state, Long userId) {
        return state.players.values().stream()
                .filter(player -> !player.userId.equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 给单个连接回错误消息。
     */
    public void sendError(WebSocketSession session, String code, String message) {
        send(session, Map.of("type", "error", "code", code, "message", message));
    }

    private void startGame(RoomState state) {
        List<QuizQuestion> questions = quizService.drawQuestions(state.totalRounds);
        if (questions.size() < state.totalRounds) {
            log.error("题库题目不足，房间 {} 需要 {} 道，实际 {}", state.roomId,
                    state.totalRounds, questions.size());
            broadcast(state, Map.of("type", "error",
                    "code", "NOT_ENOUGH_QUESTIONS", "message", "题库题目不够。"));
            return;
        }
        state.questions = questions;
        state.status = STATUS_PLAYING;
        state.currentRound = 1;
        quizService.updateRoomStatus(state.coupleId, state.roomId, STATUS_PLAYING, 1);
        startRound(state, 1);
    }

    private void startRound(RoomState state, int roundNo) {
        QuizQuestion question = state.questions.get(roundNo - 1);
        long deadlineAt = System.currentTimeMillis() + QUESTION_SECONDS * 1000L;

        RoundState round = new RoundState();
        round.roundNo = roundNo;
        round.question = question;
        round.options = parseOptions(question.getOptions());
        round.deadlineAt = deadlineAt;
        round.status = ROUND_ANSWERING;
        state.round = round;
        state.currentRound = roundNo;

        LocalDateTime now = LocalDateTime.now();
        quizService.createRound(state.coupleId, state.roomId, roundNo, question.getId(),
                now, now.plusSeconds(QUESTION_SECONDS));

        round.timeoutFuture = scheduler.schedule(
                () -> runWithTenant(state, () -> revealIfNeeded(state, roundNo)),
                QUESTION_SECONDS, TimeUnit.SECONDS);

        broadcast(state, buildQuestionPayload(state, round));
    }

    /**
     * 揭晓。定时器超时和两人都答完可能同时到达，锁加状态检查保证只揭晓一次。
     */
    private void revealIfNeeded(RoomState state, int roundNo) {
        state.lock.lock();
        try {
            RoundState round = state.round;
            if (round == null || round.roundNo != roundNo
                    || !ROUND_ANSWERING.equals(round.status)) {
                return;
            }
            round.status = ROUND_REVEALED;
            if (round.timeoutFuture != null) {
                round.timeoutFuture.cancel(false);
            }
            quizService.markRoundRevealed(state.coupleId, state.roomId, roundNo);

            Map<Long, Integer> answers = new LinkedHashMap<>();
            for (Long playerId : sortedPlayerIds(state)) {
                answers.put(playerId, round.answers.get(playerId));
            }
            boolean matched = allSame(answers);
            broadcast(state, buildRevealPayload(state, round, answers, matched));
            log.info("Quiz room {} round {} revealed, matched={}", state.roomId, roundNo, matched);

            // 3 秒后由服务端统一推进，避免两个人各自点下一题导致进度错开。
            scheduler.schedule(
                    () -> runWithTenant(state, () -> advanceRound(state, roundNo)),
                    REVEAL_SECONDS, TimeUnit.SECONDS);
        } finally {
            state.lock.unlock();
        }
    }

    /**
     * 揭晓 3 秒后推进：还有题就开下一轮，打完就结算。
     */
    private void advanceRound(RoomState state, int roundNo) {
        state.lock.lock();
        try {
            // 掉线超时可能已经结算，已排好的 3 秒推进必须放弃，不能把 finished 改回 playing。
            if (STATUS_FINISHED.equals(state.status)) {
                return;
            }
            RoundState round = state.round;
            if (round == null || round.roundNo != roundNo
                    || !ROUND_REVEALED.equals(round.status)) {
                return;
            }
            if (state.currentRound >= state.totalRounds) {
                finishGame(state);
                return;
            }
            int nextRound = state.currentRound + 1;
            state.currentRound = nextRound;
            quizService.updateRoomStatus(state.coupleId, state.roomId, STATUS_PLAYING, nextRound);
            startRound(state, nextRound);
        } finally {
            state.lock.unlock();
        }
    }

    /**
     * 结算：从数据库组装每题回顾，广播结果，房间标记 finished。
     */
    private void finishGame(RoomState state) {
        state.status = STATUS_FINISHED;
        QuizResultVO result = quizService.buildResult(state.coupleId, state.roomId);
        quizService.finishRoom(state.coupleId, state.roomId);

        Map<String, Object> payload = new LinkedHashMap<>(
                objectMapper.convertValue(result, new TypeReference<Map<String, Object>>() {
                }));
        payload.put("type", "result");
        broadcast(state, payload);
        log.info("Quiz room {} finished, score={}", state.roomId, result.getScore());
    }

    private boolean allSame(Map<Long, Integer> answers) {
        Integer first = null;
        for (Integer value : answers.values()) {
            if (value == null) {
                return false;
            }
            if (first == null) {
                first = value;
            } else if (!first.equals(value)) {
                return false;
            }
        }
        return first != null;
    }

    private Map<String, Object> buildQuestionPayload(RoomState state, RoundState round) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "question");
        payload.put("roundNo", round.roundNo);
        payload.put("total", state.totalRounds);
        payload.put("content", round.question.getContent());
        payload.put("options", round.options);
        payload.put("startedAt", round.deadlineAt - QUESTION_SECONDS * 1000L);
        payload.put("deadlineAt", round.deadlineAt);
        payload.put("serverNow", System.currentTimeMillis());
        return payload;
    }

    private Map<String, Object> buildRevealPayload(RoomState state, RoundState round,
                                                   Map<Long, Integer> answers, boolean matched) {
        List<Map<String, Object>> answerList = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : answers.entrySet()) {
            PlayerState player = state.players.get(entry.getKey());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", entry.getKey());
            item.put("nickname", player == null ? null : player.nickname);
            // 未答用 -1 表示，避免全局 Jackson 配置把 null 字段省略掉。
            item.put("optionIndex", entry.getValue() == null ? -1 : entry.getValue());
            answerList.add(item);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "reveal");
        payload.put("roundNo", round.roundNo);
        payload.put("content", round.question.getContent());
        payload.put("options", round.options);
        payload.put("answers", answerList);
        payload.put("matched", matched);
        return payload;
    }

    private List<String> parseOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("题目选项不是合法 JSON", ex);
        }
    }

    private List<Long> sortedPlayerIds(RoomState state) {
        return state.players.keySet().stream().sorted().toList();
    }

    private boolean allReady(RoomState state) {
        // 默契问答固定双人：空间里只有一个人时，一个人点准备也不能开局。
        return state.players.size() == PLAYER_COUNT
                && state.players.values().stream().allMatch(player -> player.ready);
    }

    /**
     * 定时器线程没有请求的 UserContext，手动设置给租户插件用。
     */
    private void runWithTenant(RoomState state, Runnable action) {
        try {
            UserContext.set(new LoginUser(state.createdBy, null, state.coupleId));
            action.run();
        } finally {
            UserContext.clear();
        }
    }

    private RoomState findState(WebSocketSession session) {
        Long roomId = attr(session, ATTR_ROOM_ID);
        return roomId == null ? null : rooms.get(roomId);
    }

    private RoomState loadState(QuizRoom room) {
        RoomState state = new RoomState();
        state.roomId = room.getId();
        state.coupleId = room.getCoupleId();
        state.createdBy = room.getCreatedBy();
        state.status = room.getStatus();
        state.currentRound = room.getCurrentRound();
        state.totalRounds = room.getTotalRounds();

        // 空间成员固定两个人，房间状态从这里初始化。
        List<User> members = userService.lambdaQuery()
                .eq(User::getCoupleId, room.getCoupleId())
                .orderByAsc(User::getId)
                .list();
        for (User member : members) {
            PlayerState player = new PlayerState();
            player.userId = member.getId();
            player.nickname = member.getNickname();
            state.players.put(member.getId(), player);
        }
        return state;
    }

    private Map<String, Object> buildJoinedPayload(RoomState state) {
        return Map.of("type", "joined", "room", buildRoomInfo(state));
    }

    private Map<String, Object> buildRoomInfo(RoomState state) {
        Map<String, Object> room = new LinkedHashMap<>();
        room.put("id", state.roomId);
        room.put("status", state.status);
        room.put("currentRound", state.currentRound);
        room.put("totalRounds", state.totalRounds);
        room.put("members", buildMembers(state));
        return room;
    }

    private Map<String, Object> buildReadyPayload(RoomState state) {
        List<Long> readyUserIds = state.players.values().stream()
                .filter(player -> player.ready)
                .map(player -> player.userId)
                .sorted()
                .toList();
        return Map.of(
                "type", "readyState",
                "readyUserIds", readyUserIds,
                "roomStatus", state.status
        );
    }

    private List<Map<String, Object>> buildMembers(RoomState state) {
        List<PlayerState> players = new ArrayList<>(state.players.values());
        players.sort(Comparator.comparing(player -> player.userId));
        List<Map<String, Object>> members = new ArrayList<>(players.size());
        for (PlayerState player : players) {
            Map<String, Object> member = new LinkedHashMap<>();
            member.put("userId", player.userId);
            member.put("nickname", player.nickname);
            member.put("online", player.online);
            member.put("ready", player.ready);
            members.add(member);
        }
        return members;
    }

    private void broadcast(RoomState state, Map<String, ?> payload) {
        for (PlayerState player : state.players.values()) {
            if (player.online) {
                send(player.session, payload);
            }
        }
    }

    private void send(WebSocketSession session, Map<String, ?> payload) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            session.sendMessage(new TextMessage(json));
        } catch (IOException ex) {
            log.warn("Quiz WebSocket 发送失败: {}", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T attr(WebSocketSession session, String name) {
        return (T) session.getAttributes().get(name);
    }

    /**
     * 一个房间的内存状态。
     */
    private static class RoomState {

        Long roomId;
        Long coupleId;
        Long createdBy;
        String status;
        int currentRound;
        int totalRounds;
        List<QuizQuestion> questions;
        RoundState round;
        final Map<Long, PlayerState> players = new ConcurrentHashMap<>();
        final ReentrantLock lock = new ReentrantLock();
    }

    /**
     * 当前这一轮的状态。
     */
    private static class RoundState {

        int roundNo;
        QuizQuestion question;
        List<String> options;
        long deadlineAt;
        String status;
        final Map<Long, Integer> answers = new LinkedHashMap<>();
        ScheduledFuture<?> timeoutFuture;
    }

    /**
     * 一个玩家的连接状态。
     */
    private static class PlayerState {

        Long userId;
        String nickname;
        WebSocketSession session;
        String sessionId;
        boolean online;
        boolean ready;
        ScheduledFuture<?> offlineFuture;
    }
}
