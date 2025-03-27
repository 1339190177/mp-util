package com.github.daixuyang.autoconfigure;

import com.github.daixuyang.utils.MpUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(MpUtil.class)
public class MpUtilAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MpUtil mpUtil() {
        return new MpUtil();
    }
}