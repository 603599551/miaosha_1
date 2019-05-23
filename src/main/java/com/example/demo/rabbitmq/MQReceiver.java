package com.example.demo.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @date 2019-05-23
 * 消息接收类
 */
@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);


    /**
     * Direct模式
     * @param message
     */
    @RabbitListener(queues = MQConfig.DIRECT_QUEUE)
    public void receiveDirect(String message){
        log.info("receive direct message: " + message);
    }

    /**
     * Topic模式
     * @param message
     */
    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receivetTopic1(String message){
        log.info("receive topic queue1 message: " + message);
    }
    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receivetTopic2(String message){
        log.info("receive topic queue2 message: " + message);
    }


    /**
     * headers模式
     * @param message
     */
    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
    public void receiveHeaders(byte[] message){
        log.info("receive headers queue message: " + new String(message));
    }
}
