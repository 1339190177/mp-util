package com.github.daixuyang.utils;

import cn.hutool.core.util.ClassUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 82153
 */
public class Tool {


    /**
     *  字符
     */
    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z]");

    /**
     * 驼峰转下划线
     * @param str 字符串
     * @return 转换后的字符串
     * */
    public static String humpToLine2(String str) {
        Matcher matcher = HUMP_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 是否是基本类型
     * @param clazz 属性
     * @return true or false
     */
    public static boolean isSimpleType(Class<?> clazz){
        if(clazz == null){
            return false;
        }
        if(String.class.getTypeName().equals(clazz.getTypeName())){
            return true;
        }else if(Date.class.getTypeName().equals(clazz.getTypeName())){
            return true;
        }else if(LocalDateTime.class.getTypeName().equals(clazz.getTypeName())){
            return true;
        }else if(LocalDate.class.getTypeName().equals(clazz.getTypeName())){
            return true;
        }else{
            return ClassUtil.isBasicType(clazz);
        }
    }


}
