package com.pinkdoor.photo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkdoor.photo.entity.Photo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 相册照片数据访问层。
 */
@Mapper
public interface PhotoMapper extends BaseMapper<Photo> {
}
