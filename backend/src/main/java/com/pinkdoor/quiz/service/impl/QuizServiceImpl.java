package com.pinkdoor.quiz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinkdoor.common.ResultCode;
import com.pinkdoor.common.exception.BusinessException;
import com.pinkdoor.quiz.entity.QuizAnswer;
import com.pinkdoor.quiz.entity.QuizQuestion;
import com.pinkdoor.quiz.entity.QuizRoom;
import com.pinkdoor.quiz.entity.QuizRound;
import com.pinkdoor.quiz.mapper.QuizAnswerMapper;
import com.pinkdoor.quiz.mapper.QuizQuestionMapper;
import com.pinkdoor.quiz.mapper.QuizRoomMapper;
import com.pinkdoor.quiz.mapper.QuizRoundMapper;
import com.pinkdoor.quiz.service.QuizService;
import com.pinkdoor.quiz.vo.QuizResultVO;
import com.pinkdoor.quiz.vo.QuizRoomVO;
import com.pinkdoor.user.entity.User;
import com.pinkdoor.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 默契问答实现：建房、查房。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private static final String STATUS_WAITING = "waiting";
    private static final String STATUS_PLAYING = "playing";
    private static final int TOTAL_ROUNDS = 5;

    private final QuizRoomMapper quizRoomMapper;
    private final QuizQuestionMapper quizQuestionMapper;
    private final QuizRoundMapper quizRoundMapper;
    private final QuizAnswerMapper quizAnswerMapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuizRoomVO createOrGetRoom(Long coupleId, Long userId) {
        // 同一空间只保留一个未结束的房间，两个人进入的是同一张桌子。
        QuizRoom existing = quizRoomMapper.selectOne(new LambdaQueryWrapper<QuizRoom>()
                .eq(QuizRoom::getCoupleId, coupleId)
                .in(QuizRoom::getStatus, STATUS_WAITING, STATUS_PLAYING)
                .orderByDesc(QuizRoom::getId)
                .last("LIMIT 1"));
        if (existing != null) {
            return buildRoomVO(existing);
        }

        QuizRoom room = new QuizRoom();
        room.setCoupleId(coupleId);
        room.setStatus(STATUS_WAITING);
        room.setCurrentRound(0);
        room.setTotalRounds(TOTAL_ROUNDS);
        room.setCreatedBy(userId);
        if (quizRoomMapper.insert(room) != 1) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "房间创建失败。");
        }
        log.info("Quiz room {} created by user {} in couple {}", room.getId(), userId, coupleId);
        return buildRoomVO(room);
    }

    @Override
    public QuizRoom getOwnedRoom(Long coupleId, Long roomId) {
        return quizRoomMapper.selectOne(new LambdaQueryWrapper<QuizRoom>()
                .eq(QuizRoom::getId, roomId)
                .eq(QuizRoom::getCoupleId, coupleId));
    }

    @Override
    public QuizRoomVO buildRoomVO(QuizRoom room) {
        // pd_user 在租户忽略表里，这里显式按空间过滤。
        List<User> members = userService.lambdaQuery()
                .eq(User::getCoupleId, room.getCoupleId())
                .orderByAsc(User::getId)
                .list();
        return QuizRoomVO.builder()
                .roomId(room.getId())
                .status(room.getStatus())
                .totalRounds(room.getTotalRounds())
                .members(members.stream()
                        .map(user -> QuizRoomVO.Member.builder()
                                .userId(user.getId())
                                .nickname(user.getNickname())
                                .avatar(user.getAvatar())
                                .build())
                        .toList())
                .build();
    }

    @Override
    public List<QuizQuestion> drawQuestions(int count) {
        List<QuizQuestion> questions = new ArrayList<>(quizQuestionMapper.selectList(null));
        if (questions.size() <= count) {
            return questions;
        }
        // 服务端洗牌一次，两个人拿到同一份题目。
        Collections.shuffle(questions);
        return new ArrayList<>(questions.subList(0, count));
    }

    @Override
    public void createRound(Long coupleId, Long roomId, int roundNo, Long questionId,
                            LocalDateTime startedAt, LocalDateTime deadlineAt) {
        QuizRound round = new QuizRound();
        round.setCoupleId(coupleId);
        round.setRoomId(roomId);
        round.setRoundNo(roundNo);
        round.setQuestionId(questionId);
        round.setStatus("answering");
        round.setStartedAt(startedAt);
        round.setDeadlineAt(deadlineAt);
        if (quizRoundMapper.insert(round) != 1) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "题目创建失败。");
        }
    }

    @Override
    public boolean saveAnswer(Long coupleId, Long roomId, int roundNo, Long userId, int optionIndex) {
        QuizAnswer answer = new QuizAnswer();
        answer.setCoupleId(coupleId);
        answer.setRoomId(roomId);
        answer.setRoundNo(roundNo);
        answer.setUserId(userId);
        answer.setOptionIndex(optionIndex);
        try {
            return quizAnswerMapper.insert(answer) == 1;
        } catch (DuplicateKeyException ex) {
            // 唯一键 (room_id, round_no, user_id) 拦住重复提交。
            return false;
        }
    }

    @Override
    public void markRoundRevealed(Long coupleId, Long roomId, int roundNo) {
        quizRoundMapper.update(null, new LambdaUpdateWrapper<QuizRound>()
                .eq(QuizRound::getCoupleId, coupleId)
                .eq(QuizRound::getRoomId, roomId)
                .eq(QuizRound::getRoundNo, roundNo)
                .eq(QuizRound::getStatus, "answering")
                .set(QuizRound::getStatus, "revealed"));
    }

    @Override
    public void updateRoomStatus(Long coupleId, Long roomId, String status, int currentRound) {
        quizRoomMapper.update(null, new LambdaUpdateWrapper<QuizRoom>()
                .eq(QuizRoom::getId, roomId)
                .eq(QuizRoom::getCoupleId, coupleId)
                .set(QuizRoom::getStatus, status)
                .set(QuizRoom::getCurrentRound, currentRound));
    }

    @Override
    public QuizResultVO buildResult(Long coupleId, Long roomId) {
        List<QuizRound> rounds = quizRoundMapper.selectList(new LambdaQueryWrapper<QuizRound>()
                .eq(QuizRound::getCoupleId, coupleId)
                .eq(QuizRound::getRoomId, roomId)
                .orderByAsc(QuizRound::getRoundNo));
        if (rounds.isEmpty()) {
            return QuizResultVO.builder()
                    .matched(0)
                    .total(0)
                    .score(0)
                    .text("这一局没有完成。")
                    .details(List.of())
                    .build();
        }

        List<Long> questionIds = rounds.stream().map(QuizRound::getQuestionId).toList();
        Map<Long, QuizQuestion> questions = quizQuestionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(QuizQuestion::getId, question -> question));

        List<QuizAnswer> answers = quizAnswerMapper.selectList(new LambdaQueryWrapper<QuizAnswer>()
                .eq(QuizAnswer::getCoupleId, coupleId)
                .eq(QuizAnswer::getRoomId, roomId));
        Map<Integer, Map<Long, Integer>> answersByRound = new HashMap<>();
        for (QuizAnswer answer : answers) {
            answersByRound.computeIfAbsent(answer.getRoundNo(), key -> new HashMap<>())
                    .put(answer.getUserId(), answer.getOptionIndex());
        }

        List<User> members = userService.lambdaQuery()
                .eq(User::getCoupleId, coupleId)
                .orderByAsc(User::getId)
                .list();
        Map<Long, String> nicknames = members.stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
        List<Long> playerIds = members.stream().map(User::getId).toList();

        int matched = 0;
        List<QuizResultVO.Detail> details = new ArrayList<>();
        for (QuizRound round : rounds) {
            QuizQuestion question = questions.get(round.getQuestionId());
            Map<Long, Integer> roundAnswers = answersByRound.getOrDefault(
                    round.getRoundNo(), Map.of());

            Integer first = null;
            boolean same = true;
            List<QuizResultVO.AnswerItem> answerItems = new ArrayList<>();
            for (Long playerId : playerIds) {
                Integer optionIndex = roundAnswers.get(playerId);
                if (optionIndex == null) {
                    same = false;
                } else if (first == null) {
                    first = optionIndex;
                } else if (!first.equals(optionIndex)) {
                    same = false;
                }
                answerItems.add(QuizResultVO.AnswerItem.builder()
                        .userId(playerId)
                        .nickname(nicknames.get(playerId))
                        .optionIndex(optionIndex == null ? -1 : optionIndex)
                        .build());
            }
            boolean roundMatched = same && first != null;
            if (roundMatched) {
                matched++;
            }
            details.add(QuizResultVO.Detail.builder()
                    .roundNo(round.getRoundNo())
                    .content(question == null ? "" : question.getContent())
                    .options(question == null ? List.of() : parseOptions(question.getOptions()))
                    .answers(answerItems)
                    .matched(roundMatched)
                    .build());
        }

        int total = rounds.size();
        int score = Math.round(matched * 100f / total);
        return QuizResultVO.builder()
                .matched(matched)
                .total(total)
                .score(score)
                .text(resultText(score))
                .details(details)
                .build();
    }

    @Override
    public void finishRoom(Long coupleId, Long roomId) {
        quizRoomMapper.update(null, new LambdaUpdateWrapper<QuizRoom>()
                .eq(QuizRoom::getId, roomId)
                .eq(QuizRoom::getCoupleId, coupleId)
                .set(QuizRoom::getStatus, "finished")
                .set(QuizRoom::getFinishedAt, LocalDateTime.now()));
    }

    private List<String> parseOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("题目选项不是合法 JSON", ex);
        }
    }

    private String resultText(int score) {
        if (score == 100) {
            return "天生一对，根本不用商量！";
        }
        if (score >= 60) {
            return "已经很合拍了，再练练就满分！";
        }
        if (score >= 40) {
            return "还需要再了解对方一点点～";
        }
        return "看来还得多约会，多了解对方～";
    }
}
