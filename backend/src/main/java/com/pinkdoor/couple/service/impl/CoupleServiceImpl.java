package com.pinkdoor.couple.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pinkdoor.common.ResultCode;
import com.pinkdoor.common.exception.BusinessException;
import com.pinkdoor.couple.entity.Couple;
import com.pinkdoor.couple.mapper.CoupleMapper;
import com.pinkdoor.couple.service.CoupleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 情侣空间领域服务实现。
 */
@Service
public class CoupleServiceImpl extends ServiceImpl<CoupleMapper, Couple> implements CoupleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Couple getOrCreate(String name) {
        Couple existing = lambdaQuery()
                .eq(Couple::getName, name)
                .orderByAsc(Couple::getId)
                .last("LIMIT 1")
                .one();
        if (existing != null) {
            return existing;
        }
        Couple couple = new Couple();
        couple.setName(name);
        if (!save(couple)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR.getCode(), "情侣空间创建失败。");
        }
        return couple;
    }
}
