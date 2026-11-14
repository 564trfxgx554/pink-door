package com.pinkdoor.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkdoor.quiz.entity.QuizRoom;
import org.apache.ibatis.annotations.Mapper;

/**
 * 默契问答房间数据访问层。
 */
@Mapper
public interface QuizRoomMapper extends BaseMapper<QuizRoom> {
}
