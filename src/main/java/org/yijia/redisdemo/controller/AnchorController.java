package org.yijia.redisdemo.controller;

import autumn.tools.redis.RedisHandler;
import autumn.tools.redis.ReidsPubSub;
import autumn.tools.redis.cmd.Cmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yijia.redisdemo.constants.Constants;
import redis.clients.jedis.Client;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/anchor")
public class AnchorController {

    private Logger logger = LoggerFactory.getLogger(AnchorController.class);

    @Autowired
    private RedisHandler redisHandler;

    @PostMapping("/beginToShow")
    public void beginToShow(String userId) {
        Long start = redisHandler.builder().add(Cmd.hash.hsetnx(Constants.AnchorHelp.ANCHOR_START_POOL, userId, userId)).exec(Long.class);
        if (start == 1) {
            logger.info("<-----直播已开始----->");
            redisHandler.builder().add(Cmd.set.sadd(Constants.AnchorHelp.ANCHOR, userId)).exec();
            Map<String,Object> map = new HashMap<>();
            map.put("userId",userId);
            redisHandler.publish(Constants.AnchorHelp.ANCHOR_START_POOL,map);
            logger.info("<-----加入直播列表----->");
        } else {
            logger.info("<-----您当前已经开播----->");
        }
    }

    @PostMapping("/endToShow")
    public void endToShow(String userId) {
        Long exec = redisHandler.builder().add(Cmd.hash.hdel(Constants.AnchorHelp.ANCHOR_START_POOL, userId)).exec(Long.class);
        if (exec == 1) {
            logger.info("<-----您已下波----->");
            redisHandler.builder().add(Cmd.set.srem(Constants.AnchorHelp.ANCHOR, userId)).exec();
            logger.info("<-----删除直播列表中对应主播----->");
        } else {
            logger.info("<-----您当前尚未开播----->");
        }
    }

    @GetMapping("/getAllAnchorInfo")
    public void getAllAnchorInfo(){
        ReidsPubSub reidsPubSub = new ReidsPubSub() {
            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                super.onSubscribe(channel, subscribedChannels);
            }
        };
        redisHandler.subscribe(reidsPubSub, Constants.AnchorHelp.ANCHOR_START_POOL);
    }

}
