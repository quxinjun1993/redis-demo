package org.yijia.redisdemo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class PubSubService extends JedisPubSub {
    Logger logger = LoggerFactory.getLogger(PubSubService.class);

    /**
     * 监听到订阅频道接收到消息
     */
    @Override
    public void onMessage(String channel, String message) {
        logger.info(String.format("onSubscribe: channel[%s], " + "message[%s]", channel, message));
    }

    /**
     * 监听到订阅模式接收到消息
     */
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        logger.info(String.format("onPMessage: pattern[%s], channel[%s], message[%s]", pattern, channel, message));
    }

    /**
     * 订阅频道时的回调
     *
     */
    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        System.out.println("用的这个。。。。。。。。");
        logger.info(String.format("onSubscribe: channel[%s], " + "subscribedChannels[%s]", channel, subscribedChannels));
    }

    /**
     * 取消订阅频道时的回调
     */
    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        logger.info(String.format("onUnsubscribe: channel[%s], " + "subscribedChannels[%s]", channel, subscribedChannels));
    }

    /**
     * 取消订阅模式时的回调
     */
    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        logger.info(String.format("onPUnsubscribe: pattern[%s], " + "subscribedChannels[%s]", pattern, subscribedChannels));
    }

    /**
     * 订阅频道模式时的回调
     */
    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        logger.info(String.format("onPSubscribe: pattern[%s], " + "subscribedChannels[%s]", pattern, subscribedChannels));
    }

    public static void main(String[] args) {
        Jedis jedis = null;
        try {
            jedis = new Jedis("127.0.0.1", 6379, 0);// redis服务地址和端口号
            PubSubService pubSub = new PubSubService();
            jedis.subscribe(pubSub, "anchor:start:pool");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.disconnect();
            }
        }
    }

}
