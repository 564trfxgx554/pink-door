package com.pinkdoor.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pinkdoor.ai.entity.AiConversation;
import com.pinkdoor.ai.mapper.AiConversationMapper;
import com.pinkdoor.ai.service.AiConversationService;
import org.springframework.stereotype.Service;

/**
 * AI 会话服务实现。
 */
@Service
public class AiConversationServiceImpl
        extends ServiceImpl<AiConversationMapper, AiConversation>
        implements AiConversationService {
}
