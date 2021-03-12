package org.yijia.redisdemo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {

//    String redisKey() default "";

    /**
     * 是否存缓存
     *
     * @return
     */
//    boolean isRedis() default true;

    /**
     * 存入缓存类型
     *
     * @return
     */
    String type() default "String";

    /**
     * 默认缓存时间
     *
     * @return
     */
    int seconds() default 30;

    /**
     * 是否持久缓存设置
     * ps：传入缓存的时间 * 10 ，默认（30 * 10）
     *
     * @return
     */
    boolean isDurable() default false;

}
