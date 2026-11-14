package com.pinkdoor.couple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pinkdoor.couple.entity.Couple;

/**
 * 情侣空间领域服务。
 */
public interface CoupleService extends IService<Couple> {

    /**
     * 按名称查询空间，不存在时创建。
     * 预置账号初始化时用同一个名称把两个账号归到同一个空间。
     *
     * @param name 空间名称
     * @return 已存在或新建的空间
     */
    Couple getOrCreate(String name);
}
