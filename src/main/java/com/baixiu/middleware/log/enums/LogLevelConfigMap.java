package com.baixiu.middleware.log.enums;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import java.util.Map;

/**
 * logger level config 
 * level match level 打印的占比
 * @author chenfanglin1
 * @date 创建时间 2024/4/27 10:03 PM
 */
@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "logger.level")
public class LogLevelConfigMap {
    
    private Map<String,Integer> LOGGER_LEVEL_MAP;

    public Map<String, Integer> getLOGGER_LEVEL_MAP() {
        return LOGGER_LEVEL_MAP;
    }

    public void setLOGGER_LEVEL_MAP(Map<String, Integer> LOGGER_LEVEL_MAP) {
        this.LOGGER_LEVEL_MAP = LOGGER_LEVEL_MAP;
    }
}
