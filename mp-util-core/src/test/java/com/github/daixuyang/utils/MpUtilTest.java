package com.github.daixuyang.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.daixuyang.annotation.MpQuery;
import com.github.daixuyang.utils.MpUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MpUtilTest {

    // 新增测试用例
    @Test
    void generateWrapper_ShouldHandleAllQueryTypes() {
        class MultiTypeEntity {
            @MpQuery(type = "NE")
            private Integer status = 1;

            @MpQuery(type = "LIKE_LEFT", field = "title")
            private String searchKey = "unit";

            @MpQuery(type = "IS_NULL", defaultValue = "true")
            private Boolean expired;
        }

        QueryWrapper<Object> wrapper = MpUtil.generateWrapper(new MultiTypeEntity());
        String sql = wrapper.getExpression().getNormal().toString();

        assertThat(sql)
                .contains("status <> 1")
                .contains("title LIKE 'unit%'")
                .contains("expired IS NULL");
    }

    @Test
    void shouldHandleGroupByAndOrderBy() {
        class OrderEntity {
            @MpQuery(groupBy = "category", orderBy = "create_time:DESC,price:ASC")
            private String group;
        }

        QueryWrapper<Object> wrapper = MpUtil.generateWrapper(new OrderEntity());

        assertThat(wrapper.getSqlSegment())
                .contains("GROUP BY category");

        assertThat(wrapper.getSqlSegment())
                .contains("ORDER BY create_time DESC,")
                .contains("price ASC");
    }

    @Test
    void shouldHandleCustomCondition() {
        class ConditionEntity {
            @MpQuery(type = "CONDITION",
                    condition = "price BETWEEN {min} AND {max}",
                    conditionParams = {"#{value}-10", "#{value}+10"})
            private Integer value = 100;
        }

        QueryWrapper<Object> wrapper = MpUtil.generateWrapper(new ConditionEntity());
        assertThat(wrapper.getCustomSqlSegment())
                .contains("price BETWEEN 90 AND 110");
    }
}
