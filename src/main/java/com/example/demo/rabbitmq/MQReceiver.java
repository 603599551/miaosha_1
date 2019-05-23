package com.example.demo.rabbitmq;

import com.example.demo.domain.MiaoshaOrder;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.domain.OrderInfo;
import com.example.demo.redis.RedisService;
import com.example.demo.result.CodeMsg;
import com.example.demo.result.Result;
import com.example.demo.service.GoodsService;
import com.example.demo.service.MiaoshaService;
import com.example.demo.service.MiaoshaUserService;
import com.example.demo.service.OrderService;
import com.example.demo.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @date 2019-05-23
 * 消息接收类
 */
@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    MQSender sender;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message){
        log.info("receive message: " + message);
        MiaoshaMessage mm = RedisService.stringToBean(message,MiaoshaMessage.class);
        MiaoshaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();

        //查询数据库，判断库存，防止超卖现象 @TODO （目前没有做并发，后期要补上）
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) return;

        //查询Redis缓存，判断之前是否秒杀到了商品，防止重复秒杀 （这一步是为了减少数据库的访问，防止重复秒杀的根本还是要靠数据库的唯一索引）
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order != null) return;

        //事务：减库存->下订单->写入秒杀订单
        miaoshaService.miaosha(user,goods);
    }


//    /**
//     * Direct模式
//     * @param message
//     */
//    @RabbitListener(queues = MQConfig.DIRECT_QUEUE)
//    public void receiveDirect(String message){
//        log.info("receive direct message: " + message);
//    }
//
//    /**
//     * Topic模式
//     * @param message
//     */
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
//    public void receivetTopic1(String message){
//        log.info("receive topic queue1 message: " + message);
//    }
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
//    public void receivetTopic2(String message){
//        log.info("receive topic queue2 message: " + message);
//    }
//
//
//    /**
//     * headers模式
//     * @param message
//     */
//    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
//    public void receiveHeaders(byte[] message){
//        log.info("receive headers queue message: " + new String(message));
//    }
}
