package org.yijia.redisdemo;

import autumn.tools.redis.RedisHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yijia.redisdemo.constants.Constants;
import org.yijia.redisdemo.controller.AnchorController;
import org.yijia.redisdemo.controller.InterfaceController;
import org.yijia.redisdemo.controller.RedPackageController;
import org.yijia.redisdemo.controller.RoomController;
import redis.clients.jedis.Jedis;

import java.util.Properties;

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

    @Autowired
    private InterfaceController interfaceController;

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
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    testController.getRedPackage(Thread.currentThread().getName());
                }
            });
            thread.start();
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
        roomController.inRoom("4");
    }

    @Test
    public void test_8(){
        roomController.givingGifts("2",50L);
    }

    @Test
    public void test_9(){
        roomController.outRoom("1");
    }


    @Test
    public void test_10(){
        interfaceController.getUserInfo("1","1","1");
    }

    @Test
    public void test_11(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Jedis jedis = new Jedis("127.0.0.1", 6379);
                        jedis.publish(Constants.RoomHelp.ROOM + ":9527","hello");
                    }catch (Exception e){

                    }
                }
            }
        });
    }
}
