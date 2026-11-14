package com.pinkdoor.ai.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkdoor.ai.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * AI 会话数据访问层。
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {

    /**
     * 刷新会话最后消息时间。
     * 可能在 SSE 异步回调线程执行，此时没有请求线程的 UserContext，
     * 租户插件会拼出 {@code couple_id = -1} 导致更新静默失败，所以显式跳过租户过滤。
     * conversationId 在进入异步前已经过归属校验，这里只按主键更新。
     *
     * @param conversationId 会话 ID
     * @param now            当前时间
     * @return 受影响行数
     */
    @InterceptorIgnore(tenantLine = "true")
    @Update("UPDATE pd_ai_conversation SET last_message_at = #{now} WHERE id = #{conversationId}")
    int touchLastMessageAt(@Param("conversationId") Long conversationId,
                           @Param("now") LocalDateTime now);
}
