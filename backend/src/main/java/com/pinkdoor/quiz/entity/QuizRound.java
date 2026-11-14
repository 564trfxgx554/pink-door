package com.pinkdoor.quiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 默契问答的每一轮：一个房间的第几题。
 */
@Data
@TableName("pd_quiz_round")
public class QuizRound {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long coupleId;

    private Long roomId;

    /**
     * 第几题，从 1 开始。
     */
    private Integer roundNo;

    private Long questionId;

    /**
     * answering / revealed。
     */
    private String status;

    private LocalDateTime startedAt;

    /**
     * 截止时间，服务端判断是否超时以它为准。
     */
    private LocalDateTime deadlineAt;
}
