package org.yijia.redisdemo.controller;

import autumn.tools.redis.RedisHandler;
import autumn.tools.redis.ReidsPubSub;
import autumn.tools.redis.cmd.Cmd;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yijia.redisdemo.constants.Constants;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/room")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    private static final String ROOM_ID = ":9527";

    @Autowired
    private RedisHandler redisHandler;

    public void inRoom(String userId) {
        ReidsPubSub reidsPubSub = new ReidsPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                logger.info(String.format("onSubscribe: channel[%s], " + "message[%s]", channel, message));
                JSONObject obj = JSON.parseObject(message);
                if ("1".equals(obj.get("type"))) {
                    logger.info("<------用户【" + obj.get("userId") + "】进入房间------>");
                } else if ("2".equals(obj.get("type"))) {
                    //送礼物
                    logger.info("<------用户【" + obj.get("userId") + "】向主播送出【" + obj.get("money") + "】元------>");
//                    Set set = redisHandler.builder().add(Cmd.zset.zrevrange(Constants.RoomHelp.ROOM_USER_ACTUAL + ROOM_ID, 0, -1)).exec(Set.class);
                    Set<Tuple> set = redisHandler.builder().add(Cmd.zset.zrevrangeWithScores(Constants.RoomHelp.ROOM_USER_ACTUAL + ROOM_ID, 0L, 10L)).exec(Set.class);
                    System.out.println(set);
                    logger.info("*********当前排行榜**********");
                    Iterator<Tuple> iterator = set.iterator();
                    int i = 1;
                    while (iterator.hasNext()) {
                        Tuple next = iterator.next();
                        String element = next.getElement();
                        double score = next.getScore();
                        if (score != 0.0) {
                            logger.info("{}.用户【{}】,金额【{}元】", i++, element, score);
                        }
                    }
                } else if ("3".equals(obj.get("type"))) {
                    logger.info("<------用户【" + obj.get("userId") + "】离开房间------>");
                }
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "1");
                map.put("userId", userId);
                redisHandler.publish(Constants.RoomHelp.ROOM + ROOM_ID, map);
            }

        };
//        redisHandler.subscribe(reidsPubSub, Constants.RoomHelp.ROOM);
        //从历史hash中拿出用户在当前房间充值的金额
        String oldMoney = redisHandler.builder().add(Cmd.hash.hget(Constants.RoomHelp.ROOM_USER_SAVE + ROOM_ID, userId)).exec(String.class);
        oldMoney = oldMoney == null ? "0" : oldMoney;
        //将其设置到当前所有在直播间的用户的hash中
        Long val = redisHandler.builder().add(Cmd.hash.hsetnx(Constants.RoomHelp.ROOM_USER_HOME + ROOM_ID, userId, oldMoney)).exec(Long.class);
        if (val == 1) {
            //将当前用户添加到实时数据帮中
            redisHandler.builder().add(Cmd.zset.zincrby(Constants.RoomHelp.ROOM_USER_ACTUAL + ROOM_ID, Double.parseDouble(oldMoney), userId)).exec();
            Jedis jedis = new Jedis("127.0.0.1", 6379);
            jedis.subscribe(reidsPubSub, Constants.RoomHelp.ROOM + ROOM_ID);
//            redisHandler.subscribe(reidsPubSub,Constants.RoomHelp.ROOM + ROOM_ID);
        } else {
            logger.info("<------您已在房间内，操作出错！------>");
        }
    }

    /**
     * 送礼物
     *
     * @param userId 用户id
     * @param money  金额
     */
    public void givingGifts(String userId, Long money) {
        if (money <= 0) {
            logger.info("<------你在逗我？------>");
        } else {
            Long exec = redisHandler.builder().add(Cmd.hash.hincrBy(Constants.RoomHelp.ROOM_USER_HOME + ROOM_ID, userId, money)).exec(Long.class);
            if (exec > 0) {
                redisHandler.builder().add(Cmd.zset.zincrby(Constants.RoomHelp.ROOM_USER_ACTUAL + ROOM_ID, money, userId)).exec();

                Map<String, Object> map = new HashMap<>();
                map.put("type", "2");
                map.put("userId", userId);
                map.put("money", money);
                redisHandler.publish(Constants.RoomHelp.ROOM + ROOM_ID, map);
            }
        }
    }

    /**
     * 离开房间
     *
     * @param userId 用户id
     */
    public void outRoom(String userId) {
        //同步数据
        //删除排行榜信息
        Long exec = redisHandler.builder().add(Cmd.zset.zrem(Constants.RoomHelp.ROOM_USER_ACTUAL + ROOM_ID, userId)).exec(Long.class);
        if (exec == 1) {
            String currentMoney = redisHandler.builder().add(Cmd.hash.hget(Constants.RoomHelp.ROOM_USER_HOME + ROOM_ID, userId)).exec(String.class);
            //本次充值金额
            redisHandler.builder().add(Cmd.hash.hdel(Constants.RoomHelp.ROOM_USER_HOME + ROOM_ID, userId)).exec(Long.class);
//        redisHandler.builder().add(Cmd.hash.hdel(Constants.RoomHelp.ROOM_USER_SAVE + ROOM_ID, userId)).exec();
            redisHandler.builder().add(Cmd.hash.hincrBy(Constants.RoomHelp.ROOM_USER_SAVE + ROOM_ID, userId, Long.parseLong(currentMoney))).exec();
            Map<String, Object> map = new HashMap<>();
            map.put("type", "3");
            map.put("userId", userId);
            map.put("info", "");
            redisHandler.publish(Constants.RoomHelp.ROOM + ROOM_ID, map);
            logger.info("在本直播间总消费:[{}]元。", currentMoney);
        }
    }


}
