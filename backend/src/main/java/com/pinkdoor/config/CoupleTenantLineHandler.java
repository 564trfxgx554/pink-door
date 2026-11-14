package com.pinkdoor.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.pinkdoor.security.UserContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 情侣空间租户处理器：给所有带 couple_id 的业务表自动追加空间条件。
 * 业务代码正常查询即可，插件负责在 SQL 层加上 {@code couple_id = 当前空间}。
 */
@Component
public class CoupleTenantLineHandler implements TenantLineHandler {

    /**
     * 不参与租户过滤的表：
     * pd_user / pd_couple：登录和初始化阶段访问，此时还没有租户上下文；
     * pd_ai_message：没有 couple_id，归属由父会话校验；
     * pd_quiz_question：全局共享题库，不属于任何情侣。
     */
    private static final Set<String> IGNORE_TABLES = Set.of(
            "pd_user",
            "pd_couple",
            "pd_ai_message",
            "pd_quiz_question"
    );

    @Override
    public Expression getTenantId() {
        Long coupleId = UserContext.getCoupleId();
        // fail-closed：没有租户上下文时用 -1，查不到任何数据，绝不放行全表。
        return new LongValue(coupleId == null ? -1L : coupleId);
    }

    @Override
    public String getTenantIdColumn() {
        return "couple_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return IGNORE_TABLES.contains(tableName.toLowerCase());
    }
}
