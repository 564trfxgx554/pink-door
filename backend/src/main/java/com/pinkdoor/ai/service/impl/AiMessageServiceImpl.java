package com.pinkdoor.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pinkdoor.ai.entity.AiMessage;
import com.pinkdoor.ai.mapper.AiMessageMapper;
import com.pinkdoor.ai.service.AiMessageService;
import org.springframework.stereotype.Service;

/**
 * AI 消息服务实现。
 */
@Service
public class AiMessageServiceImpl
        extends ServiceImpl<AiMessageMapper, AiMessage>
        implements AiMessageService {
}
