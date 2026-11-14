package com.pinkdoor.quiz.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 默契问答题库，全局共享，不按情侣隔离。
 */
@Data
@TableName("pd_quiz_question")
public class QuizQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    /**
     * JSON 数组字符串，例如 ["火锅","奶茶","蛋糕","烧烤"]。
     */
    private String options;

    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
