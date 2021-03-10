package org.yijia.redisdemo.controller;

import autumn.tools.base.j2se.MapHandler;
import autumn.tools.base.secret.MD5;
import autumn.tools.redis.RedisCmdBuilder;
import autumn.tools.redis.RedisHandler;
import autumn.tools.redis.cmd.Cmd;
import org.nutz.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yijia.redisdemo.constants.Constants;
import org.yijia.redisdemo.entity.UserEntity;
import org.yijia.redisdemo.utils.PackageUtils;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/redPackage")
public class RedPackageController {

    private Logger logger = LoggerFactory.getLogger(RedPackageController.class);

    @Autowired
    private RedisHandler redisHandler;

    @GetMapping("/test")
    public void test() {
        String a = redisHandler.builder().add(Cmd.str.get("a")).exec(String.class);
        logger.info("redis中取出的值：{}", a);

        Map<String, String> hash = new HashMap<String, String>();
        hash.put("name", "test");
        hash.put("sex", "1");
        redisHandler.builder().add(Cmd.hash.hmset("user:6", hash)).exec();

        List<String> result = redisHandler.builder().add(Cmd.hash.hmget("user:6", "name", "sex")).exec(List.class);
        logger.info("hash结果集：{}", result);

        Map<String, String> result2 = redisHandler.builder().add(Cmd.hash.hgetall("user:6")).exec(Map.class);
        logger.info("hash结果集：{}", result2);

        UserEntity user = MapHandler.map2Obj(result2, UserEntity.class);
        logger.info("转换的User对象：name:{},sex:{}", user.getName(), user.getSex());

//        MapHandler;
//        StringHandler;
//        ListHandler;
//        Json;
//        DateHandler
//        RpcServer
    }

    public static void main(String[] args) {
        System.out.println(MD5.getMD5String("aaaaaa"));
        System.out.println(MD5.md5("aaaaaa"));
        System.out.println(MD5.randomString());
    }

    /**
     * 获取手气最佳
     *
     * @param flag
     */
    @PostMapping("/getUserPackageRank")
    public void getUserPackageRank(Boolean flag) {
        Long result = redisHandler.builder().add(Cmd.list.llen(Constants.PackageHelp.PACKAGE_POOL)).exec(Long.class);
        if (result != 0) {
            logger.info("<----------相同红包池尚未领完---------->");
            if (!flag) {
                return;
            }
        }
        Set<String> set = redisHandler.builder().add(Cmd.zset.zrevrange(Constants.PackageHelp.USER_RANK, 0, 0)).exec(Set.class);
        logger.info("结果集:{}", set);
        set.iterator().forEachRemaining(obj -> {
            logger.info("<----------手气最佳：id为【{}】的用户---------->", obj);
        });
    }

    /**
     * 抢红包
     *
     * @param userId 用户id
     * @return
     */
    @PostMapping("/getRedPackage")
    public String getRedPackage(String userId) {
        //通过 setnx 判断用户是否已经抢过红包
        Long ok = redisHandler.builder().add(Cmd.hash.hsetnx(Constants.PackageHelp.USER_PACKAGE_POOL, userId, "0.0")).exec(Long.class);
        logger.info("<----------判断用户是否已经抢过红包---------->");
        if (ok == 1) {
            logger.info("<----------未抢---------->");
            //从红包池中弹出一个红包给用户
            String money = redisHandler.builder().add(Cmd.list.lpop(Constants.PackageHelp.PACKAGE_POOL)).exec(String.class);
            if (!Strings.isEmpty(money)) {
                logger.info("<----------红包池弹出一个红包---------->");
                //将用户添加进hash中
                redisHandler.builder().add(Cmd.hash.hset(Constants.PackageHelp.USER_PACKAGE_POOL, userId, money)).exec();
                logger.info("<----------将领取了红包的用户添加至hash---------->");
                //添加进 zset
                redisHandler.builder().add(Cmd.zset.zadd(Constants.PackageHelp.USER_RANK, Double.parseDouble(money), userId)).exec();
                logger.info("<----------将领了红包的用户添加至zset---------->");
                logger.info("<----------恭喜你，抢了【" + money + "】元---------->");
                return "恭喜你，抢了【" + money + "】元";
            } else {
                logger.info("对不起，红包已领完");
                return "对不起，红包已领完";
            }
        } else {
            logger.info("<----------已抢---------->");
            return "对不起，您已经抢过了！";
        }
    }

    /**
     * 随机设置红包
     *
     * @param money 红包总金额
     * @param count 拆分个数
     * @return
     */
    @PostMapping("/setRedPackage")
    public void setRedPackage(Integer money, Integer count) {
        //        logger.error("随机：{}", randDivide(money, count));
//        logger.error("平均：{}", averageDivide(money, count));
        List<Double> packageList = new ArrayList<>();
//        AtomicReference<Double> all = new AtomicReference<>(0.0);
        PackageUtils.randDivide(money, count).forEach(obj -> {
            double val = new BigDecimal(obj).divide(new BigDecimal(100)).doubleValue();
//            all.updateAndGet(v -> new Double((double) (v + val)));
            packageList.add(val);
        });
        logger.info("最后的结果：{}", packageList);
        redisHandler.builder().add(Cmd.list.lpush(Constants.PackageHelp.PACKAGE_POOL, PackageUtils.transStringArray(packageList))).exec();
        redisHandler.builder().add(Cmd.key.del(Constants.PackageHelp.USER_PACKAGE_POOL)).exec();
        redisHandler.builder().add(Cmd.key.del(Constants.PackageHelp.USER_RANK)).exec();
        logger.info("<----------红包池已经设定---------->");

    }

}
