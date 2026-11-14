package com.pinkdoor.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 唯一的预置账号实体，明文密码不会进入该类。
 */
@Data
@TableName("pd_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属情侣空间，多租户的边界字段。
     */
    private Long coupleId;

    private String nickname;

    @JsonIgnore
    private String password;

    private String avatar;

    /**
     * 时间戳由 MySQL 维护（DEFAULT CURRENT_TIMESTAMP / ON UPDATE），
     * MyBatis-Plus 更新时禁止写回，否则 updated_at 不会自动刷新。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
