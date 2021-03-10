package org.yijia.redisdemo.controller;

import autumn.tools.redis.RedisHandler;
import autumn.tools.redis.ReidsPubSub;
import autumn.tools.redis.cmd.Cmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yijia.redisdemo.constants.Constants;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/anchor")
public class AnchorController {

    private Logger logger = LoggerFactory.getLogger(AnchorController.class);

    @Autowired
    private RedisHandler redisHandler;

    @GetMapping("/beginToShow/{userId}")
    public void beginToShow(@PathVariable("userId") String userId) {
        Long start = redisHandler.builder().add(Cmd.hash.hsetnx(Constants.AnchorHelp.ANCHOR_START_POOL, userId, userId)).exec(Long.class);
        if (start == 1) {
            logger.info("<-----直播已开始----->");
            redisHandler.builder().add(Cmd.set.sadd(Constants.AnchorHelp.ANCHOR, userId)).exec();
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("online", "on");
            redisHandler.publish(Constants.AnchorHelp.ANCHOR_START_POOL, map);
            logger.info("<-----加入直播列表----->");
        } else {
            logger.info("<-----您当前已经开播----->");
        }
    }

    @GetMapping("/endToShow/{userId}")
    public void endToShow(@PathVariable("userId") String userId) {
        Long exec = redisHandler.builder().add(Cmd.hash.hdel(Constants.AnchorHelp.ANCHOR_START_POOL, userId)).exec(Long.class);
        if (exec == 1) {
            logger.info("<-----您已下波----->");
            redisHandler.builder().add(Cmd.set.srem(Constants.AnchorHelp.ANCHOR, userId)).exec();
            logger.info("<-----删除直播列表中对应主播----->");
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("online", "down");
            redisHandler.publish(Constants.AnchorHelp.ANCHOR_START_POOL, map);
        } else {
            logger.info("<-----您当前尚未开播----->");
        }
    }

    public void getAllAnchorInfo() {
        ReidsPubSub reidsPubSub = new ReidsPubSub() {

            @Override
            public void onMessage(String channel, String message) {
                logger.info(String.format("onSubscribe: channel[%s], " + "message[%s]", channel, message));
                Set<String> exec = redisHandler.builder().add(Cmd.set.smembers(Constants.AnchorHelp.ANCHOR)).exec(Set.class);
                logger.info("<------当前直播列表------>");
                Iterator<String> iterator = exec.iterator();
                while (iterator.hasNext()) {
                    logger.info("<------" + iterator.next() + "------>");
                }
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                logger.info("<------当前已开启实时通道------>");
            }
        };
//        redisHandler.subscribe(reidsPubSub, Constants.AnchorHelp.ANCHOR_START_POOL);
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.subscribe(reidsPubSub, Constants.AnchorHelp.ANCHOR_START_POOL);
    }

}
