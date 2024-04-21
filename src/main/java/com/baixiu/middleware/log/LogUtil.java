package com.baixiu.middleware.log;

import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author baixiu
 * @date 2024年04月21日
 */
@Component
public class LogUtil {
    

    /**
     * tracerLog
     */
    private static Logger TRACER_LOGGER = LoggerFactory.getLogger("TRACER_LOGGER");
    

    static Type logConfigMapType = (new TypeReference<Map<String, Integer>>() {}).getType();
    
    static final Random random = new Random();

    public LogUtil() {
    }

    protected static boolean isLogOn(LogLevelEnum loglevel, Logger logger) {
        boolean result = false;
        if (isConfigOn(loglevel, logger)) {
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

    protected static boolean isConfigOn(LogLevelEnum loglevel, Logger logger) {
        try {
            if (loglevel == null) {
                return false;
            } else {
                Map<String, Integer> config = (Map) configCenter.getTracerConfig("logger.config", (Object) null, logConfigMapType);
                if (config != null && !config.isEmpty()) {
                    Integer range = (Integer) config.get(LogLevelEnum.ALL.name());
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
            }
        } catch (Exception var4) {
            return true;
        }
    }

    private static void trace(Logger logger, String message, Object... arg) {
        if (isLogOn(LogLevelEnum.TRACE, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.debug(message, arg[0]);
            } else {
                logger.debug(message, arg);
            }
        }

    }

    private static void debug(Logger logger, String message, Object... arg) {
        if (isLogOn(LogLevelEnum.DEBUG, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.debug(message, arg[0]);
            } else {
                logger.debug(message, arg);
            }
        }

    }

    private static void info(Logger logger, String message, Object... arg) {
        if (isLogOn(LogLevelEnum.INFO, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.info(message, arg[0]);
            } else {
                logger.info(message, arg);
            }
        }

    }

    private static void warn(Logger logger, String message, Object... arg) {
        if (isLogOn(LogLevelEnum.WARN, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.warn(message, arg[0]);
            } else {
                logger.warn(message, arg);
            }
        }

    }

    private static void error(Logger logger, String message, Object... arg) {
        if (isLogOn(LogLevelEnum.ERROR, logger)) {
            if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                logger.error(message, arg[0]);
            } else {
                logger.error(message, arg);
            }

            TRACER_LOGGER.error(message, arg);
        }
    }

    public static void error(Object... arg) {
        String message = getMessage("{}", 4, arg);
        error(getSoaErrorLogger(), message, arg);
    }

    public static void error(String message, Object... arg) {
        message = getMessage(message, 4, arg);
        error(getSoaErrorLogger(), message, arg);
    }

    public static void checkAndPrintError(Throwable e) {
        checkAndPrintError(e.getMessage(), e);
    }

    public static void checkAndPrintError(String mess, Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            e = cause;
        }
        try {
            //У���Ƿ�Ϊ�ܾ߳̾������쳣
            if (e instanceof RejectedExecutionException) {
                mess = getMessage(mess + " rejected.", 4, e);//��ȡmessage
                error(mess, new Object[0]);
            }
        } catch (Exception exception) {
            error("checkAndPrintError error:" + mess);
        }
    }

    public static void exception(String message, Object... arg) {        
        if(configCenter.isBusinessSwitchOn ("logger.errorWithRealMessage.switch.on")){
            final String realMessage=message;
            message = getMessage(message, 4, arg);
            errorWithRealMessage(realMessage,getSoaErrorLogger(),message,arg); 
        }else{
            message = getMessage(message, 4, arg);
            error(getSoaErrorLogger(), message, arg);
        }        
    }

    public static void requestInfo(String message, Object... arg) {
        info(getSoaRequestLogger(), message, arg);
    }

    public static void tracerInfo(String message, Object... arg) {
        info(TRACER_LOGGER, message, arg);
    }

    public static void requestDebug(String message, Object... arg) {
        debug(getSoaRequestLogger(), message, arg);
    }

    public static void serverSideWarn(Class<?> type, String message, Object... arg) {
        message = getMessage(message, 4, arg);
        warn(LoggerFactory.getLogger(type), message, arg);
    }

    public static void serverSideDebug(Class<?> type, String message, Object... arg) {
        debug(LoggerFactory.getLogger(type), message, arg);
    }

    public static void serverSideInfo(Class<?> type, String message, Object... arg) {

        info(LoggerFactory.getLogger(type), message, arg);
    }

    public static void serverSideTrace(Class<?> type, String message, Object... arg) {
        trace(LoggerFactory.getLogger(type), message, arg);
    }


    public static void setSoaRequestLogger(Logger soaRequestLogger) {
        soaRequestLogger = soaRequestLogger;
    }



    public static void setSoaErrorLogger(Logger soaErrorLogger) {
        soaErrorLogger = soaErrorLogger;
    }



    public static enum LogLevelEnum {
        ALL,
        FATAL,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE;

        private LogLevelEnum() {
        }
    }

   
    /**
     * get message 
     */
    public static String getMessage(String message, int index, Object... arg) {
        try {
            if(StringUtils.isBlank(message)){
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

    private static void getError(Exception e){
        try {
            boolean errorOn = configCenter.isBusinessSwitchOn("CartCommonLogHelper.error.on");
            if(!errorOn){
                error("CartCommonLogHelper.getError :",e);
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
            if(isLogOn(LogLevelEnum.ERROR, logger)){
                if(isPassAway(realMessage)){
                    if (arg != null && arg.length > 0 && arg[0] instanceof Throwable) {
                        logger.error(message, arg[0]);
                    } else {
                        logger.error(message, arg);
                    }
                    TRACER_LOGGER.error(message, arg);
                }
            }
        } catch (Exception e) {
            error ("errorWithRealMessage.error.",e);
        }
    }
}
