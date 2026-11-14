package com.pinkdoor.quiz.controller;

import com.pinkdoor.common.Result;
import com.pinkdoor.common.ResultCode;
import com.pinkdoor.common.exception.BusinessException;
import com.pinkdoor.quiz.entity.QuizRoom;
import com.pinkdoor.quiz.service.QuizService;
import com.pinkdoor.quiz.vo.QuizRoomVO;
import com.pinkdoor.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 默契问答 REST 接口：只做建房和查房，玩的过程全部走 WebSocket。
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizRoomController {

    private final QuizService quizService;

    /**
     * 创建或复用当前空间的房间。
     */
    @PostMapping("/rooms")
    public Result<QuizRoomVO> createRoom() {
        return Result.success(quizService.createOrGetRoom(
                UserContext.getCoupleId(), UserContext.getUserId()));
    }

    /**
     * 查询房间，跨空间返回 404。
     */
    @GetMapping("/rooms/{roomId}")
    public Result<QuizRoomVO> getRoom(@PathVariable Long roomId) {
        QuizRoom room = quizService.getOwnedRoom(UserContext.getCoupleId(), roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "房间不存在。");
        }
        return Result.success(quizService.buildRoomVO(room));
    }
}
