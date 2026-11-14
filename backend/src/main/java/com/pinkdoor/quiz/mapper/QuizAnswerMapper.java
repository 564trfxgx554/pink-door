package com.pinkdoor.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkdoor.quiz.entity.QuizAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 默契问答答案数据访问层。
 */
@Mapper
public interface QuizAnswerMapper extends BaseMapper<QuizAnswer> {
}
