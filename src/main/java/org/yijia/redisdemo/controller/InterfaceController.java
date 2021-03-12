package org.yijia.redisdemo.controller;

import autumn.tools.redis.RedisHandler;
import com.google.common.collect.Maps;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yijia.redisdemo.annotations.MyAnnotation;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/interface")
public class InterfaceController {

    @Autowired
    private RedisHandler redisHandler;

    @RequestMapping("/getUserInfo")
    @MyAnnotation(type = "Map", seconds = 30, isDurable = true)
    public Map<String, String> getUserInfo(String name, String id, String sex) {
        System.out.println("当前进的控制器获取的数据");
        Map<String, String> map = Maps.newHashMap();
        map.put("id", id);
        map.put("name", name);
        map.put("sex", sex);
        return map;
    }

    @RequestMapping("/getNewUserInfo")
    @MyAnnotation(type = "String", seconds = 30, isDurable = true)
    public String getNewUserInfo(String name, String id, String sex) {
        Map<String, String> map = Maps.newHashMap();
        map.put("id", id);
        map.put("name", name);
        map.put("sex", sex);
        return Json.toJson(map);
    }

    public void test() throws IOException {
        /**
         * org.yijia.redisdemo.controller.InterfaceController
         */
        System.out.println(this.getClass().getName());
        System.out.println(this.getClass().getCanonicalName());

        System.out.println(this.getClass().getResource("/"));
        System.out.println(this.getClass().getClassLoader());
        System.out.println(this.getClass().getClassLoader().getResources(this.getClass().getName()).hasMoreElements());
    }

    public static void main(String[] args) throws IOException {
        InterfaceController a = new InterfaceController();
        a.test();
    }
}
