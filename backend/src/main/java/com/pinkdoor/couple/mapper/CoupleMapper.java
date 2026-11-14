package com.pinkdoor.couple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkdoor.couple.entity.Couple;
import org.apache.ibatis.annotations.Mapper;

/**
 * 情侣空间数据访问层。
 */
@Mapper
public interface CoupleMapper extends BaseMapper<Couple> {
}
