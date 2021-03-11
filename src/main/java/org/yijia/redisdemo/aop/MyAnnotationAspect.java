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
    public Map<String, String> around(ProceedingJoinPoint pjp, MyAnnotation my) {
        int i = pjp.getSignature().toString().lastIndexOf(" ");
        String str = pjp.getSignature().toString().substring(i + 1);
        String key = str.substring(0, str.indexOf("(")).replaceAll("\\.", ":");
        Object[] args = pjp.getArgs();
        for (Object arg : args) {
            key += ":" + arg;
        }
        System.err.println("around方法 key:" + key);

        long start = System.currentTimeMillis();
        boolean redis = my.isRedis();
//        if (redis) {
//            Map<String, String> result = redisHandler.builder().add(Cmd.hash.hgetall(my.redisKey())).exec(Map.class);
//            System.out.println("缓存中:" + result);
//            if (!result.isEmpty()) {
//                long end = System.currentTimeMillis();
//                System.out.println("方法执行时间：" + (end - start));
//                return result;
//            }
//        }
        try {
            //从redis中取
            if (redis) {
                if ("Map".equals(my.type())) {
                    Map<String, String> result = redisHandler.builder().add(Cmd.hash.hgetall(key)).exec(Map.class);
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            }
            Object obj = pjp.proceed();
            //存入redis
            if ("Map".equals(my.type())) {
                redisHandler.builder().add(Cmd.hash.hmset(key, (Map<String, String>) obj)).exec();
                redisHandler.builder().add(Cmd.key.expire(key, my.seconds())).exec();
            }
            long end = System.currentTimeMillis();
            System.out.println("方法执行时间：" + (end - start));
            return null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }
}
