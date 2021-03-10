package org.yijia.redisdemo.redis.config;

import autumn.tools.redis.RedisHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RedisConfig {

    @Value("${local.redis.handler.name}")
    private String name;
    @Value("${local.redis.handler.host}")
    private String host;
    @Value("${local.redis.handler.port}")
    private Integer port;
    @Value("${local.redis.handler.timeout}")
    private Integer timeout;
    @Value("${local.redis.handler.password}")
    private String password;
    @Value("${local.redis.handler.minIdle}")
    private Integer minIdle;
    @Value("${local.redis.handler.maxIdle}")
    private Integer maxIdle;
    @Value("${local.redis.handler.maxActive}")
    private Integer maxActive;


    @Bean
    public RedisHandler getRedisHandler() {
        return new RedisHandler(name, host, port, timeout, password, minIdle, maxIdle, maxActive);
    }

}
