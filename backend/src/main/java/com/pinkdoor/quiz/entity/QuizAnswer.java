package com.pinkdoor.quiz.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 默契问答答案，一人一题一行。
 */
@Data
@TableName("pd_quiz_answer")
public class QuizAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long coupleId;

    private Long roomId;

    private Integer roundNo;

    private Long userId;

    /**
     * 选项下标，从 0 开始。
     */
    private Integer optionIndex;

    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
