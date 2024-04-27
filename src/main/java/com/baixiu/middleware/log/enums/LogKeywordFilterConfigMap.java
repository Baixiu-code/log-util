package com.baixiu.middleware.log.enums;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * logger keyword filter
 * 如果配置了则不进行打印
 * @author chenfanglin1
 * @date 创建时间 2024/4/27 10:03 PM
 */
@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "logger.keyword.filter")
public class LogKeywordFilterConfigMap {
    
    public Set<String> filterKeyWords;

    public Set<String> getFilterKeyWords() {
        filterKeyWords=new HashSet<> ();
        if(StringUtils.isNotBlank(filters)){
            filterKeyWords.addAll(Arrays.asList(filters.split (",")));
        }
        return filterKeyWords;
    }

    @Getter@Setter
    private String filters;
    
    
}
