package com.pinkdoor.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkdoor.quiz.entity.QuizRound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 默契问答轮次数据访问层。
 */
@Mapper
public interface QuizRoundMapper extends BaseMapper<QuizRound> {
}
