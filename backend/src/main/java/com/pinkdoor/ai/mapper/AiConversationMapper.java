package com.pinkdoor.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkdoor.ai.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 会话数据访问层。
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
}
