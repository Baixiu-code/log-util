package com.baixiu.middleware.log.core;

import com.alibaba.fastjson.TypeReference;
import com.baixiu.middleware.log.enums.LogLevelConfigMap;
import com.baixiu.middleware.log.enums.LoggerLevelEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.RejectedExecutionException;

/**
 * 
 * @author baixiu
 * @date 2024年04月21日
 */
@Setter
@Getter
@Component
public class LogCoreUtil {
    
    private static Logger LOGGER = LoggerFactory.getLogger("LOGGER");
    private static Logger ERROR_LOGGER = LoggerFactory.getLogger("LOGGER-ERROR");
    private static Logger REQUEST_LOGGER = LoggerFactory.getLogger("LOGGER-REQUEST");
    
    static Type logConfigMapType = (new TypeReference<Map<String, Integer>>() {}).getType();
    
    static final Random random = new Random();

    /**
     * 堆栈打印深度
     */
    @Value("${stackLineNum}")
    private Integer stackLineNum;

    /**
     * 是否打印异常日志
     */
    @Value("${logger.errorOn}")
    private boolean errorOn;

    @Autowired
    private LogLevelConfigMap logLevelConfigMap;

    
    

    public LogCoreUtil() {
    }

    /**
     * 指定的日志级别是否开启日志打印
     * @param loglevel 日志级别
     * @param logger logger 对象
     * @return
     */
    protected  boolean isOpenByLogLevel(LoggerLevelEnum loglevel, Logger logger) {
        boolean result = false;
        if (isConfigOn(loglevel,logger)) {
            switch (loglevel) {
                case INFO:
                    result = logger.isInfoEnabled();
                    break;
                case WARN:
                    result = logger.isWarnEnabled();
                    break;
                case DEBUG:
                    result = logger.isDebugEnabled();
                    break;
                case ERROR:
                    result = logger.isErrorEnabled();
                    break;
                case FATAL:
                    result = logger.isErrorEnabled();
                    break;
                case TRACE:
                    result = logger.isTraceEnabled();
                    break;
                case ALL:
                default:
                    result = logger.isErrorEnabled();
            }
        }
        return result;
    }

    protected  boolean isConfigOn(LoggerLevelEnum loglevel, Logger logger) {
        try {
            //为空 不打开
            if (loglevel == null) {
                return false;
            }
            Map<String, Integer> config = logLevelConfigMap.getLOGGER_LEVEL_MAP ();
            if (config != null && !config.isEmpty()) {
                Integer range = (Integer) config.get(LoggerLevelEnum.ALL.name());
                if (range == null) {
                    range = (Integer) config.get(logger.getName());
                }

                if (range == null) {
                    range = (Integer) config.get(loglevel.name());
                }

                return range == null ? true : (range.intValue() >= 0 && range.intValue() < 100 ? (range.intValue() == 0 ? false : random.nextInt(100) < range.intValue()) : true);
            } else {
                return true;
            }
        } catch (Exception var4) {
            return true;
        }
    }

    private void trace(Logger logger, String message, Object... arg) {
        if (isOpenByLogLevel (LoggerLevelEnum.TRACE, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.debug(message, arg[0]);
            } else {
                logger.debug(message, arg);
            }
        }

    }

    private void debug(Logger logger, String message, Object... arg) {
        if (isOpenByLogLevel (LoggerLevelEnum.DEBUG, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.debug(message, arg[0]);
            } else {
                logger.debug(message, arg);
            }
        }

    }

    private void info(Logger logger, String message, Object... arg) {
        if (isOpenByLogLevel (LoggerLevelEnum.INFO, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.info(message, arg[0]);
            } else {
                logger.info(message, arg);
            }
        }

    }

    private void warn(Logger logger, String message, Object... arg) {
        if (isOpenByLogLevel (LoggerLevelEnum.WARN, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.warn(message, arg[0]);
            } else {
                logger.warn(message, arg);
            }
        }

    }

    private void error(Logger logger, String message, Object... arg) {
        if (isOpenByLogLevel (LoggerLevelEnum.ERROR, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.error(message, arg[0]);
            } else {
                logger.error(message, arg);
            }
            LOGGER.error(message, arg);
        }
    }

   

    public void error(String message, Object... arg) {
        message = getMessage(message, 4, arg);
        error(ERROR_LOGGER, message, arg);
    }

    /**
     * check 方法用以首先做一次类型校验后进行指定类型异常的打印。
     * 好处是可以进行指定的异常打印。
     * @param e throw able 
     */
    public void checkAndPrintError(Throwable e) {
        checkAndPrintError(e.getMessage(), e);
    }

    public void checkAndPrintError(String mess, Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            e = cause;
        }
        try {
            if (e instanceof RejectedExecutionException) {
                mess = getMessage(mess + " rejected.", 4, e);//��ȡmessage
                error(mess, new Object[0]);
            }
        } catch (Exception exception) {
            error("checkAndPrintError error:" + mess);
        }
    }

    public  void requestInfo(String message, Object... arg) {
        info(REQUEST_LOGGER, message, arg);
    }

    public  void tracerInfo(String message, Object... arg) {
        info(LOGGER, message, arg);
    }

    public  void serverSideInfo(Class<?> type, String message, Object... arg) {
        info(LoggerFactory.getLogger(type), message, arg);
    }

  


    /**
     * get message 
     */
    public static String getMessage(String message, int index, Object... arg) {
        try {
            if(Objects.isNull(message) || message.trim().isEmpty()){
                message = "";
            }
            int occupations = StringUtils.countMatches(message, "{");
            if(occupations != arg.length){
                int needOccupations = arg.length - occupations;
                StringBuffer stringBuffer = new StringBuffer(message);
                while(needOccupations-- > 0){
                    stringBuffer.append(",{} ");
                }
                message = stringBuffer.toString();
            }

            if(arg != null){
                for (Object ar : arg) {
                    if (ar instanceof Throwable) {
                        Exception exception = (Exception) ar;
                        message = message + getStackTraceStr(exception);
                    }
                }
            }            
        } catch (Exception e) {
            getError(e);
            return "getMessage error:"+e.getMessage();
        }
        return message;
    }

    private void getError(Exception e){
        try {
            if(!errorOn){
                error("LogCoreUtil.getError:",e);
            }
        }catch (Exception exception){
            
        }
    }

    /**
     * 根据堆栈深度printLog
     * 可支持自定义堆栈深度
     */
    public static String getStackTraceStr(Exception exception) {
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        if(stackTraceElements != null){
            int stackTraceLineNum = configCenter.getBusinessConfigValueByKey("stack_line_num", 1, Integer.class);
            stackTraceLineNum = stackTraceLineNum > stackTraceElements.length ? stackTraceElements.length : stackTraceLineNum;
            String stackTraceStr = "";
            for(int i = 0;i < stackTraceLineNum;i++){
                StackTraceElement stackTraceElement = stackTraceElements[i];
                stackTraceStr = stackTraceStr + "--StackTrace+"+i+"--ClassName:"+stackTraceElement.getClassName()
                        +  ",MethodName:"+stackTraceElement.getMethodName()+ ",lineNum:"+stackTraceElement.getLineNumber()+";";
            }
            return stackTraceStr;
        }
        return "";
    }

    /**
     * 是否支持过滤日志关键字
     * @param message 过滤的具体日志关键字
     * @return false 打印。true 不打印
     */
    private static boolean isPassAway(String message) {
        try {
            //具体的配置关键字集合
            HashSet<String> logFilters=configCenter.getBusinessConfigValueByKey("log_word_filter", new HashSet<>() , HashSet.class);
            return !logFilters.contains(message);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * error with real msg
     * @param realMessage 日志关键字
     * @param logger logger
     * @param message 实际拼接的日志字符串
     * @param arg 其他参数
     */
    private static void errorWithRealMessage(String realMessage,Logger logger, String message, Object... arg) {
        try {
            if(isOpenByLogLevel (LogLevelEnum.ERROR, logger)){
                if(isPassAway(realMessage)){
                    if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                        logger.error(message, arg[0]);
                    } else {
                        logger.error(message, arg);
                    }
                    LOGGER.error(message, arg);
                }
            }
        } catch (Exception e) {
            error ("errorWithRealMessage.error.",e);
        }
    }
}
