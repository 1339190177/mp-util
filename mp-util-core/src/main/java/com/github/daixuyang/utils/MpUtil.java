package com.github.daixuyang.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.AbstractChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.daixuyang.annotation.MpQuery;
import com.github.daixuyang.constant.QueryStatic;
import java.util.stream.Collectors;
import com.github.daixuyang.constant.QueryType;
import org.apache.logging.log4j.util.Strings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author 小代
 */
@Slf4j
public class MpUtil<T> extends AbstractChainWrapper<T, SFunction<T, ?>, LambdaQueryChainWrapper<T>, LambdaQueryWrapper<T>> {

    /**
     * 构建实体类selectLambda
     * @param <T> 数据库实体类
     * @return 条件构造器
     */
    public static <T> LambdaQueryWrapper<T> selectEntityWrapper() {
        return new QueryWrapper<T>().lambda();
    }

    /**
     * 构建实体类updateLambda
     * @param <T> 数据库实体类
     * @return 条件构造器
     */
    public static <T> LambdaUpdateWrapper<T> updateEntityWrapper() {
        return new UpdateWrapper<T>().lambda();
    }

    public static QueryWrapper<Object> generateWrapper(Object o) {
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        generateWrapper(o, wrapper);
        return wrapper;
    }

    /**
     * 将驼峰转为对应数据库的下划线字段的查询表达式
     *
     * @param o       实体类
     * @param wrapper 条件构造器
     */
    public static void generateWrapper(Object o, QueryWrapper<Object> wrapper) {
        try {
            Field[] allFields = ReflectUtil.getFields(o.getClass());

            for (Field field : allFields) {
                field.setAccessible(true);
                // 过滤static属性
                if(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())){
                    continue;
                }
                // 过滤非基本类型
                if(!isSimpleType(field.getType())){
                    continue;
                }

                // 数据表字段
                String column = QueryStatic.DEFAULT_PREFIX + humpToLine2(field.getName());

                // 值
                Object value = field.get(o);
                //默认值
                // 有注解的情况
                if (field.isAnnotationPresent(MpQuery.class)) {
                    MpQuery annotation = field.getAnnotation(MpQuery.class);
                    String type = annotation.type();

                    //优先使用自定义表前缀和字段属性
                    column = annotation.prefix() + QueryStatic.POINT + humpToLine2(field.getName());

                    //优先使用自定义字段
                    column = getField(column, annotation);

                    //有默认值重写默认值
                    value = getDefaultValue(annotation, value);

                    // 处理排序和分组
                    processOrderAndGroup(wrapper, annotation);

                    switch (type) {
                        case QueryType.EQ:
                            wrapper.eq(ObjectUtils.isNotEmpty(value), column, value);
                            break;
                        case QueryType.NE:
                            wrapper.ne(ObjectUtils.isNotEmpty(value), column, value);
                            break;
                        case QueryType.LIKE:
                            wrapper.like(ObjectUtils.isNotEmpty(value), column, value);
                            break;
                        case QueryType.LIKE_LEFT:
                            wrapper.likeLeft(ObjectUtils.isNotEmpty(value), column, value);
                            break;
                        case QueryType.LIKE_RIGHT:
                            wrapper.likeRight(ObjectUtils.isNotEmpty(value), column, value);
                            break;
                        case QueryType.LE:
                            wrapper.le(ObjectUtils.isNotEmpty(value), column, value);
                            break;
                        case QueryType.LT:
                            wrapper.lt(ObjectUtils.isNotEmpty(value), column, value);
                            break;
                        case QueryType.GE:
                            wrapper.ge(ObjectUtils.isNotEmpty(value), column, value);
                            break;
                        case QueryType.GT:
                            wrapper.gt(ObjectUtils.isNotEmpty(value), column, value);
                            break;
                        case QueryType.IS_NULL:
                            wrapper.isNull(StringUtils.isNotBlank(annotation.defaultValue()), column);
                            break;
                        case QueryType.IS_NOT_NULL:
                            wrapper.isNotNull(ObjectUtils.isNotEmpty(value), column);
                            break;
                        case QueryType.IS_EMPTY:
                            wrapper.eq(column, QueryStatic.EMPTY);
                            break;
                        case QueryType.CUSTOMIZE:
                            wrapper.apply(!ObjectUtils.isNull(value), String.valueOf(value));
                            break;
                        case QueryType.CONDITION:
                            if (StringUtils.isNotBlank(annotation.condition())) {
                                applyCondition(wrapper, annotation, value);
                            }
                            break;
                        case QueryType.IS_NOT_EMPTY:
                            wrapper.ne(column, QueryStatic.EMPTY);
                            break;
                        default:
                            break;
                    }
                } else {
                    wrapper.eq(ObjectUtils.isNotEmpty(value), column, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to generate query wrapper", e);
        }
    }

    private static void processOrderAndGroup(QueryWrapper<Object> wrapper, MpQuery annotation) {
        // 处理排序
        if (StringUtils.isNotBlank(annotation.orderBy())) {
            String[] orders = annotation.orderBy().split(",");
            for (String order : orders) {
                String trimmed = order.trim();
                if (trimmed.isEmpty()) continue;

                String[] parts = trimmed.split(":");
                if (parts.length == 2) {
                    if ("DESC".equalsIgnoreCase(parts[1])) {
                        wrapper.orderByDesc(parts[0]);
                    } else {
                        wrapper.orderByAsc(parts[0]);
                    }
                } else {
                    wrapper.orderByAsc(trimmed);
                }
            }
        }

        // 处理分组
        if (StringUtils.isNotBlank(annotation.groupBy())) {
            wrapper.groupBy(Arrays.stream(annotation.groupBy().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList())
            );
        }
    }

    private static void applyCondition(QueryWrapper<Object> wrapper, MpQuery annotation, Object value) {
        if (value == null) return;

        String condition = annotation.condition();
        if (annotation.conditionParams().length > 0) {
            Object[] params = Arrays.stream(annotation.conditionParams())
                    .map(param -> {
                        String strValue = StrUtil.toString(value);
                        return param.replace("#{value}", strValue);
                    })
                    .filter(StrUtil::isNotBlank)
                    .toArray();
            wrapper.apply(condition, params);
        } else {
            wrapper.apply(condition);
        }
    }

    private static String getField(String column, MpQuery annotation) {
        // 指定的字段
        String fd = annotation.field();
        if (Strings.isNotBlank(fd)) {
            column = annotation.prefix() + QueryStatic.POINT + humpToLine2(fd);
        }
        return column;
    }

    private static Object getDefaultValue(MpQuery annotation, Object value) {
        if (Strings.isNotBlank(annotation.defaultValue())) {
            try {
                String defaultValue = annotation.defaultValue();
                if (value == null) {
                    return defaultValue;
                }
                if (value instanceof Number) {
                    return Double.parseDouble(defaultValue);
                } else if (value instanceof Boolean) {
                    return Boolean.parseBoolean(defaultValue);
                }
                return defaultValue;
            } catch (Exception e) {
                log.warn("Failed to parse default value: {}", e.getMessage());
            }
        }
        return value;
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type == String.class ||
                Number.class.isAssignableFrom(type) ||
                type == Boolean.class;
    }

    private static String humpToLine2(String str) {
        return str.replaceAll("([A-Z])", "_$1").toLowerCase();
    }
}