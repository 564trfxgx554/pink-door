package com.pinkdoor.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkdoor.quiz.entity.QuizQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 默契问答题库数据访问层。
 */
@Mapper
public interface QuizQuestionMapper extends BaseMapper<QuizQuestion> {
}
