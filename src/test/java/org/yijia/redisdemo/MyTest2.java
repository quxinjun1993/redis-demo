package org.yijia.redisdemo;

import autumn.tools.redis.RedisHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yijia.redisdemo.controller.AnchorController;
import org.yijia.redisdemo.controller.RedPackageController;
import org.yijia.redisdemo.controller.RoomController;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MyTest2 {

    @Autowired
    private RedPackageController testController;

    @Autowired
    private AnchorController anchorController;

    @Autowired
    private RoomController roomController;

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







    @Test
    public void test_4(){
        anchorController.getAllAnchorInfo();
    }

    @Test
    public void test_5(){
        anchorController.beginToShow("1");
    }

    @Test
    public void test_6(){
        anchorController.endToShow("1");
    }


    @Test
    public void test_7(){
        roomController.inRoom("1");
    }

    @Test
    public void test_8(){
        roomController.givingGifts("2",100L);
    }

    @Test
    public void test_9(){
        roomController.outRoom("1");
    }


}
