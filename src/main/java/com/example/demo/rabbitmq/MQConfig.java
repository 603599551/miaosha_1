package com.example.demo.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @date 2019-05-23
 * rabbitMQ配置类
 */
@Configuration
public class MQConfig {

    public static final String MIAOSHA_QUEUE = "miaosha.queue";

    public static final String DIRECT_QUEUE = "direct.queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADERS_QUEUE = "headers.queue";
    public static final String TOPIC_EXCHANGE = "topicExchange";
    public static final String ROUTING_KEY1 = "topic.key1";
    //topic模式支持通配符
    public static final String ROUTING_KEY2 = "topic.#";
    public static final String FANOUT_EXCHANGE = "fanoutExchange";
    public static final String HEADERS_EXCHANGE = "headersExchange";

    /**
     * Direct模式
     * 直接采用default exchange
     */
    @Bean
    public Queue directQueue(){
        //队列名称、是否可持久化
        //true：持久化队列，队列的声明会存放到Erlang自带的Mnesia数据库中，所以该队列在服务器重新启动后继续存在。
        //false：非持久化队列，队列的声明会存放到内存中，当服务器关闭时内存会被清除，所以该队列在服务器重新启动后不存在。
        return new Queue(MIAOSHA_QUEUE,true);
    }

//
//    /**
//     * Topic模式
//     */
//    @Bean
//    public Queue topicQueue1(){
//        return new Queue(TOPIC_QUEUE1,true);
//    }
//    @Bean
//    public Queue topicQueue2(){
//        return new Queue(TOPIC_QUEUE2,true);
//    }
//    @Bean
//    public TopicExchange topicExchange(){
//        return new TopicExchange(TOPIC_EXCHANGE);
//    }
//    @Bean
//    public Binding topicBinding1(){
//        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with(ROUTING_KEY1);
//    }
//    @Bean
//    public Binding topicBinding2(){
//        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with(ROUTING_KEY2);
//    }
//
//    /**
//     * Fanout模式
//     * 广播模式--没有ROUTING_KEY的限制
//     */
//    @Bean
//    public FanoutExchange fanoutExchange(){
//        return new FanoutExchange(FANOUT_EXCHANGE);
//    }
//    @Bean
//    public Binding fanoutBinding1(){
//        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
//    }
//    @Bean
//    public Binding fanoutBinding2(){
//        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
//    }
//
//    /**
//     * Headers模式
//     */
//    @Bean
//    public Queue headersQueue(){
//        return new Queue(HEADERS_QUEUE,true);
//    }
//    @Bean
//    public HeadersExchange headersExchange(){
//        return new HeadersExchange(HEADERS_EXCHANGE);
//    }
//    @Bean
//    public Binding headersBinding(){
//        Map<String,Object> map = new HashMap<>();
//        map.put("header1","value1");
//        map.put("header2","value2");
//        return BindingBuilder.bind(headersQueue()).to(headersExchange()).whereAll(map).match();
//    }

}
