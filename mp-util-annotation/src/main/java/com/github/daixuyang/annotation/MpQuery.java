package com.github.daixuyang.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * mybatis push 注解
 *
 * @author 代旭杨
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MpQuery {

    /**
     * 表前缀
     *
     * @return 表前缀
     */
    String prefix() default "t";

    /**
     * 查询方式
     *
     * @return 查询类型
     */
    String type() default "eq";

    /**
     * 字段
     * 用户实体类中间转换查询
     *
     * @return 最后的字段
     */
    String field() default "";


    /**
     * 默认值
     *
     * @return 默认值
     */
    String defaultValue() default "";


}
