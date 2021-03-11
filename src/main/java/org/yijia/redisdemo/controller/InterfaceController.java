package org.yijia.redisdemo.controller;

import autumn.tools.redis.RedisHandler;
import autumn.tools.redis.cmd.Cmd;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yijia.redisdemo.annotations.MyAnnotation;
import org.yijia.redisdemo.constants.Constants;

import java.util.Map;

@RestController
@RequestMapping("/interface")
public class InterfaceController {

    @Autowired
    private RedisHandler redisHandler;

    @RequestMapping("/getUserInfo")
    @MyAnnotation(isRedis = true, redisKey = Constants.InterfaceHelp.USER_INFO)
    public Map<String, String> getUserInfo() {
        Map<String, String> map = Maps.newHashMap();
        map.put("id", "1");
        map.put("name", "quxinjun");
        redisHandler.builder().add(Cmd.hash.hmset(Constants.InterfaceHelp.USER_INFO, map)).exec();
        redisHandler.builder().add(Cmd.key.expire(Constants.InterfaceHelp.USER_INFO, 30)).exec();
        System.out.println("控制器中：" + map);
        return map;
    }

    public static void main(String[] args) {

    }
}
