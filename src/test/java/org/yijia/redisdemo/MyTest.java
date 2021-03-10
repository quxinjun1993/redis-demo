package org.yijia.redisdemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.text.DecimalFormat;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MyTest {

    private static final Jedis jedis = new Jedis("127.0.0.1", 6379);

    @Test
    public void test_1() {
        //设置key和value   设置成功返回OK
        String set = jedis.set("quxinjun", "屈新竣");
        System.out.println("set -------- >:" + set);
    }

    @Test
    public void test_2() {
        //取出对应key值的value
        String val = jedis.get("quxinjun");
        System.out.println("val -------- >:" + val);
    }

    @Test
    public void test_3() {
        //设置指定key的值并把之前的key的值返回出来
        String val2 = jedis.getSet("quxinjun", "屈新竣——3");
        System.out.println("val2 -------- >:" + val2);
    }

    @Test
    public void test_4() {
        //设置key和value还有过期时间  时间单位：秒
        String setex = jedis.setex("adu", 10, "adu");
        System.out.println("setex -------- >:" + setex);
    }

    @Test
    public void test_5() {
        //设置key和value还有过期时间  时间单位：毫秒
        String psetex = jedis.psetex("adu", 10, "adu");
        System.out.println("psetex -------- >:" + psetex);
    }

    @Test
    public void test_6() {
        //设置分布式锁的关键命令
        //当redis中没有对应key  则会创建一个对应的key和值，并且会返回1
        //当redis中有对应的key值  则不会对redis进行操作，并返回0
        Long setnx = jedis.setnx("lock", "lock");
        System.out.println("setnx -------- >:" + setnx);
    }

    @Test
    public void test_7() {
        //删除指定key  如果删除成功返回1  删除失败返回0
        Long del_num = jedis.del("lock");
        System.out.println("del_num -------- >:" + del_num);
    }

    @Test
    public void test_8() {
        //对指定key递增
        Long subscript_incr = jedis.incr("subscript");
        System.out.println("subscript_incr -------- >:" + subscript_incr);
    }

    @Test
    public void test_9() {
        //对指定key递减（可以为负数）
        Long subscript_decr = jedis.decr("subscript");
        System.out.println("subscript_decr -------- >:" + subscript_decr);
    }

    @Test
    public void test_10() {
        Long lpush = jedis.lpush("push_test", "a");
        System.out.println("lpush -------- >:" + lpush);
    }

    @Test
    public void test_11() {
        Long rpush = jedis.rpush("push_test", "z");
        System.out.println("rpush -------- >:" + rpush);
    }

    @Test
    public void test_13() {
        String removeElement = jedis.lpop("push_test");
        System.out.println("removeElement -------- >:" + removeElement);
    }

    @Test
    public void test_14() {
        String removeElement = jedis.rpop("push_test");
        System.out.println("removeElement -------- >:" + removeElement);
    }

    @Test
    public void test_15() {
        List<String> push_test = jedis.lrange("push_test", 0, 9);
        for (String str : push_test) {
            System.out.println(str);
        }
    }

    @Test
    public void test_16() {
        //返回集合长度
        Long length = jedis.llen("push_test");
        System.out.println("length -------- >:" + length);
    }

    @Test
    public void test_17() {
        String str = jedis.lindex("push_test", 0);
        System.out.println("str -------- >:" + str);
    }

    @Test
    public void test_18() {
        //返回值返回的是添加的成功的    覆盖原来的值  返回0
        Map<String, String> map = new HashMap<String, String>();
        map.put("7", "9");
        Long result = jedis.hset("map", map);
        System.out.println("result -------- >:" + result);
    }

    @Test
    public void test_19() {
        //返回的是对应key的值
        String val = jedis.hget("map", "1");
        System.out.println("val -------- >:" + val);
    }

    @Test
    public void test_20() {
        //无序   操作成功返回的是ok
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i <= 100; i++) {
            map.put(i + "", i + "");
        }
        String hmset = jedis.hmset("mapTest", map);
        System.out.println("hmset -------- >:" + hmset);
    }


    @Test
    public void test_21() {
        String[] arrs = new String[101];
        for (int i = 0; i <= 100; i++) {
            arrs[i] = i + "";
        }
        List<String> result = jedis.hmget("mapTest", arrs);
        result.forEach(obj -> {
            System.out.println(obj);
        });
//        System.out.println(result.size());
    }

    @Test
    public void test_22() {
        Boolean flag = jedis.hexists("mapTest", "101");
        System.out.println(flag);
    }

    @Test
    public void test_23() {
        //操作到了数据的条数
        Long val = jedis.hdel("mapTest", "0");
        System.out.println("val -------- >:" + val);
    }

    @Test
    public void test_24() {
        Map<String, String> mapTest = jedis.hgetAll("mapTest");
        System.out.println(mapTest);
        Set<Map.Entry<String, String>> entries = mapTest.entrySet();
        for (Map.Entry<String, String> map : entries) {
            System.out.println(map.getKey() + "------>" + map.getValue());
        }
    }

    @Test
    public void test_25() {
        Long size = jedis.hlen("mapTest");
        System.out.println("size -------- >:" + size);
    }

    @Test
    public void test() {
//        DecimalFormat df = new DecimalFormat( "0.00" );
//        double v = new Random().nextDouble();
//        String format = df.format(v);
//        System.out.println(format);

//        initData();

        int n = 100;
        double[] datas = new double[n];
//int sum = 1000;
        Random random = new Random();
        for (int i = 0; i < n; i = i + 2) {
            double j = random.nextDouble() * 10;
            datas[i] = j;
            datas[i + 1] = (20 - j);
        }
//计算datas的和
        double temp = 0;
        for (int i = 0; i < datas.length; i++) {
            System.out.println(datas[i]);
            temp += datas[i];
        }
        System.out.println("数组datas的和为：" + temp);


    }

    private static final String RED_PACKAGE = "red_package";
    private static final String MAP = "already_list";

    private double[] getRed() {
        int n = 100;
        double[] datas = new double[n];
        Random random = new Random();
        for (int i = 0; i < n; i = i + 2) {
            double j = random.nextDouble() * 10;
            datas[i] = j;
            datas[i + 1] = (20 - j);
        }
        return datas;
    }

    @Test
    public void initData() {
        String[] members = new String[100];
        DecimalFormat df = new DecimalFormat("0.00");
        double all = 0.0;
        double[] reds = getRed();
        for (int i = 0; i < members.length; i++) {
            all += reds[i];
            members[i] = df.format(reds[i]);
        }
        Long val = jedis.lpush(RED_PACKAGE, members);
        System.out.println("val ----->" + val);
    }


    @Test
    public void test_red() {
        for (int i = 1; i <= 100; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Boolean isHave = jedis.hexists(MAP, Thread.currentThread().getName());
                    System.out.println("当前用户是否存在map中：" + isHave);
                    if (!isHave) {
                        String redValue = jedis.rpop(RED_PACKAGE);
                        System.out.println("当前用户--->" + Thread.currentThread().getName() + ",抢到红包：" + redValue + "元");
//                        System.out.println("目前红包池还有：" + jedis.llen(RED_PACKAGE) + "个红包");
                        jedis.hset(MAP, Thread.currentThread().getName(), redValue);
                    }
                    jedis.close();
                }
            }, "thread--->" + i);
//            System.out.println(thread.getName());
            thread.start();
        }
    }
}
