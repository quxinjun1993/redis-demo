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
//    @Before("@annotation(my)")
    public Object around(ProceedingJoinPoint pjp, MyAnnotation my) {
        long start = System.currentTimeMillis();
        boolean redis = my.isRedis();
        if (redis) {
            Map<String, String> result = redisHandler.builder().add(Cmd.hash.hgetall(my.redisKey())).exec(Map.class);
            System.out.println("缓存中:" + result);
            if (!result.isEmpty()) {
                long end = System.currentTimeMillis();
                System.out.println("方法执行时间：" + (end - start));
                return result;
            }
        }
        try {
            pjp.proceed();
            long end = System.currentTimeMillis();
            System.out.println("方法执行时间：" + (end - start));
            return null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }
}
