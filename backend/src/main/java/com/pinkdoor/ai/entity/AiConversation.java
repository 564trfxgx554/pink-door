package com.pinkdoor.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 会话。
 */
@Data
@TableName("pd_ai_conversation")
public class AiConversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属情侣空间，租户外层。
     */
    private Long coupleId;

    /**
     * 所属用户，空间内私有，另一个人看不到。
     */
    private Long userId;

    private String title;

    private LocalDateTime lastMessageAt;

    /**
     * 时间戳由 MySQL 维护，更新时禁止写回。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
