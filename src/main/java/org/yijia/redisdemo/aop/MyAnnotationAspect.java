package org.yijia.redisdemo.aop;

import autumn.tools.redis.RedisHandler;
import autumn.tools.redis.cmd.Cmd;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yijia.redisdemo.annotations.MyAnnotation;
import org.yijia.redisdemo.constants.Constants;

import java.util.Locale;
import java.util.Map;

@Component
@Aspect
public class MyAnnotationAspect {

    private static final Logger logger = LoggerFactory.getLogger(MyAnnotationAspect.class);

    @Autowired
    private RedisHandler redisHandler;

    @Pointcut(value = "@annotation(org.yijia.redisdemo.annotations.MyAnnotation)")
    public void access() {
    }

    @Around("@annotation(my)")
    public Object around(ProceedingJoinPoint pjp, MyAnnotation my) {
        long start, end;
        start = System.currentTimeMillis();
        String key = getCacheKey(pjp);
        try {
            Object cacheObject = getCacheObject(key, my.type());
            Object obj = null;
            //判断cache的有效性
            boolean isValidate = isValidateCache(key, my.isDurable());
            logger.info("当前验证有效值：{}", isValidate);
            if (isValidate) {
                //验证有效
                end = System.currentTimeMillis();
                logger.info("方法执行时间：{}", getExecuteMethodTime(start, end));
                logger.info("返回的缓存对象");
                return cacheObject;
            } else {
                //验证无效
                if (cacheObject != null && my.isDurable()) {
                    logger.info("************当前持久缓存需要重启线程，更新缓存信息*************");
                    //如果无效  但是缓存并没有清理  先返还   再重置
                    String validateKey = key + ":" + Constants.InterfaceHelp.KEY_VALIDATE_SUFFIX;
                    Long exec = redisHandler.builder().add(Cmd.str.setnx(key + ":" + Constants.InterfaceHelp.KEY_NX_SUFFIX, "1")).exec(Long.class);
                    if (exec == 1) {
//                        obj = pjp.proceed();
//                        Thread thread = new UpdateCacheThread(key, obj, redisHandler, my, validateKey);
//                        thread.start();
                        new Thread(() -> {
                            try {
                                System.err.println("************异步开始*************");
                                Object result = pjp.proceed(pjp.getArgs());
                                System.err.println("异步执行返回的对象:" + result);
                                saveCacheObject(key, my.type(), (my.seconds() * 10), result, my.isDurable());
                                System.err.println("更新缓存完毕");
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        }).start();
                    }
                    redisHandler.builder().add(Cmd.key.del(key + ":" + Constants.InterfaceHelp.KEY_NX_SUFFIX)).exec();
                    end = System.currentTimeMillis();
                    logger.info("方法执行时间：{}", getExecuteMethodTime(start, end));
                    logger.info("返回的缓存对象");
                    Thread.sleep(1000);
                    return cacheObject;
                }
            }
            obj = pjp.proceed(pjp.getArgs());
            int saveTime = my.seconds();
            if (my.isDurable()) {
                saveTime = saveTime * 10;
            }
            saveCacheObject(key, my.type(), saveTime, obj, my.isDurable());
            end = System.currentTimeMillis();
            logger.info("方法执行时间：{}", getExecuteMethodTime(start, end));
            logger.info("返回的对象");
            return obj;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    /**
     * 验证key的有效性
     *
     * @param key       验证的key
     * @param isDurable 是否持久缓存
     * @return
     */
    private boolean isValidateCache(String key, Boolean isDurable) {
        Long exec = redisHandler.builder().add(Cmd.key.ttl(key)).exec(Long.class);
        logger.info("当前key的距离失效时间：{}", exec);
        if (!isDurable) {
            //不需要持久   判断是否过期
            if (exec > 0) {
                return true;
            }
        } else {
            //真实存活的时间
            String realTimeStr = redisHandler.builder().add(Cmd.str.get(key + ":" + Constants.InterfaceHelp.KEY_VALIDATE_SUFFIX)).exec(String.class);
            logger.info("当前key的真实存活时间：{}", realTimeStr);
            if (realTimeStr != null) {
                Integer realTime = Integer.parseInt(realTimeStr);
                boolean flag = (realTime * 10) - exec < realTime;
                logger.info("在设置持久的情况下是否有效：{}", flag);
                return flag;
            }
        }
        return false;
    }


    private void saveCacheObject(String key, String inType, int saveTime, Object obj, Boolean isFlag) {
        if (Constants.InterfaceHelp.SAVE_MAP.equals(inType.toLowerCase(Locale.ROOT))) {
            redisHandler.builder().add(Cmd.hash.hmset(key, (Map<String, String>) obj)).exec();
            redisHandler.builder().add(Cmd.key.expire(key, saveTime)).exec();
        }
        //设置持久缓存   保存key真实过期时间
        if (isFlag) {
            redisHandler.builder().add(Cmd.str.set(key + ":" + Constants.InterfaceHelp.KEY_VALIDATE_SUFFIX, (saveTime / 10) + "")).exec();
        }
    }

    private Object getCacheObject(String key, String inType) {
        if (Constants.InterfaceHelp.SAVE_MAP.equals(inType.toLowerCase(Locale.ROOT))) {
            Map<String, String> result = redisHandler.builder().add(Cmd.hash.hgetall(key)).exec(Map.class);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return null;
    }

    /**
     * 自动获取缓存key
     * 类路径 + 方法名 + 参数值
     * eg:org.yijia.redisdemo.controller + getUserInfo + 1:1:1
     *
     * @param pjp
     * @return
     */
    private String getCacheKey(ProceedingJoinPoint pjp) {
        int i = pjp.getSignature().toString().lastIndexOf(" ");
        String str = pjp.getSignature().toString().substring(i + 1);
        String key = str.substring(0, str.indexOf("(")).replaceAll("\\.", ":");
        Object[] args = pjp.getArgs();
        for (Object arg : args) {
            key += ":" + arg;
        }
        return key;
    }

    private Long getExecuteMethodTime(Long start, Long end) {
        return (end - start);
    }
}

/**
 * 设置新的线程重置缓存信息
 */
class UpdateCacheThread extends Thread {
    private RedisHandler redisHandler;
    private String key;
    private Object obj;
    private MyAnnotation my;
    private String validateKey;
    private ProceedingJoinPoint pjp;

    @Override
    public void run() {
        try {
            System.err.println(key);
            System.err.println("当前线程执行了。。。。。。。");
            //重置
            Object obj = pjp.proceed();
            int saveTime = my.seconds();
            if (Constants.InterfaceHelp.SAVE_MAP.equals(my.type().toLowerCase(Locale.ROOT))) {
                redisHandler.builder().add(Cmd.hash.hmset(key, (Map<String, String>) obj)).exec();
                redisHandler.builder().add(Cmd.key.expire(key, (saveTime * 10))).exec();
                System.err.println("数据重置了。。。。。。。");
            }
            //设置持久缓存   保存key真实过期时间
            if (my.isDurable()) {
                redisHandler.builder().add(Cmd.str.set(validateKey, saveTime + "")).exec();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (!this.isInterrupted()) {
                this.interrupt();
            }
            System.err.println("线程进入了异常。。。。。");
        }
    }

    public UpdateCacheThread(String key, Object obj, RedisHandler redisHandler, MyAnnotation my, String validateKey) {
        this.key = key;
        this.obj = obj;
        this.redisHandler = redisHandler;
        this.my = my;
        this.validateKey = validateKey;
    }

    public UpdateCacheThread(String key, ProceedingJoinPoint pjp, RedisHandler redisHandler, MyAnnotation my, String validateKey) {
        this.key = key;
        this.pjp = pjp;
        this.redisHandler = redisHandler;
        this.my = my;
        this.validateKey = validateKey;
    }

}
