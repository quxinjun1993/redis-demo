package org.yijia.redisdemo;

import autumn.tools.redis.RedisHandler;
import autumn.tools.redis.cmd.Cmd;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yijia.redisdemo.controller.RedPackageController;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MyTest2 {

    @Autowired
    private RedPackageController testController;

    @Autowired
    private RedisHandler redisHandler;

    @Test
    public void test() {

    }

    @Test
    public void test_1() {
        testController.setRedPackage(200, 20);
    }


    @Test
    public void test_2() {
        for (int i = 1; i <= 50; i++) {
            testController.getRedPackage(i + "");
        }
    }

    @Test
    public void test_3() {
        testController.getUserPackageRank(true);
    }
}
