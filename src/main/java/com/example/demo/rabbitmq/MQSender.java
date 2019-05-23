package com.example.demo.rabbitmq;

import com.example.demo.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @date 2019-05-23
 * 消息发送类
 */
@Service
public class MQSender {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    AmqpTemplate amqpTemplate;

    public void sendMiaoshaMessage(MiaoshaMessage mm) {
        String msg = RedisService.beanToString(mm);
        log.info("send message: " + msg);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);
    }

//    /**
//     * Direct模式
//     * @param message
//     */
//    public void sendDirect(Object message){
//        String msg = RedisService.beanToString(message);
//        log.info("send direct message: " + msg);
//        amqpTemplate.convertAndSend(MQConfig.DIRECT_QUEUE,msg);
//    }
//
//    /**
//     * Topic模式
//     * @param message
//     */
//    public void sendTopic(Object message){
//        String msg = RedisService.beanToString(message);
//        log.info("send topic message: " + msg);
//        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY1,msg + "1");
//        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key2",msg + "2");
//    }
//
//    /**
//     * Fanout模式
//     * @param message
//     */
//    public void sendFanout(Object message){
//        String msg = RedisService.beanToString(message);
//        log.info("send fanout message: " + msg);
//        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",msg);
//    }
//
//    /**
//     * Headers模式
//     * @param message
//     */
//    public void sendHeaders(Object message){
//        String msg = RedisService.beanToString(message);
//        log.info("send headers message: " + msg);
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("header1","value1");
//        properties.setHeader("header2","value2");
//        Message obj = new Message(msg.getBytes(),properties);
//        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE,"",obj);
//    }


}
