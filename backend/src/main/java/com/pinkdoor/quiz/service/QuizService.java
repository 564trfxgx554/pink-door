package com.pinkdoor.quiz.service;

import com.pinkdoor.quiz.entity.QuizQuestion;
import com.pinkdoor.quiz.entity.QuizRoom;
import com.pinkdoor.quiz.vo.QuizResultVO;
import com.pinkdoor.quiz.vo.QuizRoomVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 默契问答门面：房间与游戏流程。
 */
public interface QuizService {

    /**
     * 创建或复用当前空间的房间。
     * 同一空间同时只允许一个 waiting / playing 房间，重复调用返回同一个。
     *
     * @param coupleId 当前情侣空间
     * @param userId   创建人
     * @return 房间信息
     */
    QuizRoomVO createOrGetRoom(Long coupleId, Long userId);

    /**
     * 按空间校验并返回房间。
     *
     * @return 房间；不存在或不属于当前空间时返回 {@code null}
     */
    QuizRoom getOwnedRoom(Long coupleId, Long roomId);

    /**
     * 组装房间响应：状态 + 空间成员。
     */
    QuizRoomVO buildRoomVO(QuizRoom room);

    /**
     * 随机抽题，不重复。题库是全局共享的。
     *
     * @param count 需要几道
     * @return 抽中的题目
     */
    List<QuizQuestion> drawQuestions(int count);

    /**
     * 开一轮，写入 pd_quiz_round。
     */
    void createRound(Long coupleId, Long roomId, int roundNo, Long questionId,
                     LocalDateTime startedAt, LocalDateTime deadlineAt);

    /**
     * 保存答案。依靠唯一键防重复，重复提交返回 false。
     */
    boolean saveAnswer(Long coupleId, Long roomId, int roundNo, Long userId, int optionIndex);

    /**
     * 标记一轮已揭晓。
     */
    void markRoundRevealed(Long coupleId, Long roomId, int roundNo);

    /**
     * 更新房间状态和当前题号。
     */
    void updateRoomStatus(Long coupleId, Long roomId, String status, int currentRound);

    /**
     * 从数据库组装结果页数据。
     */
    QuizResultVO buildResult(Long coupleId, Long roomId);

    /**
     * 结算房间，状态改为 finished。
     */
    void finishRoom(Long coupleId, Long roomId);
}
