package com.pinkdoor.quiz.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 默契问答房间，一局游戏一条记录。
 */
@Data
@TableName("pd_quiz_room")
public class QuizRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属情侣空间，租户边界。
     */
    private Long coupleId;

    /**
     * waiting / playing / finished。
     */
    private String status;

    /**
     * 当前题号，0 表示还没开始。
     */
    private Integer currentRound;

    private Integer totalRounds;

    private Long createdBy;

    /**
     * 由数据库默认值维护，更新时不写回。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;
}
