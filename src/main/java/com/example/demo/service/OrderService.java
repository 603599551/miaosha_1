package com.example.demo.service;

import com.example.demo.dao.GoodsDao;
import com.example.demo.dao.OrderDao;
import com.example.demo.domain.MiaoshaOrder;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.domain.OrderInfo;
import com.example.demo.redis.OrderKey;
import com.example.demo.redis.RedisService;
import com.example.demo.result.CodeMsg;
import com.example.demo.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    //订单状态：0新建未支付，1待发货，2已发货，3已收货，4已退款，5已完成
    public static final int NOT_PAID = 0;
    public static final int NOT_SHIPPED = 1;
    public static final int HAVE_SHIPPED = 2;
    public static final int HAVE_RECEIVED = 3;
    public static final int HAVE_REFUNDED = 4;
    public static final int HAVE_FINISHED = 5;


    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    /**
     * 查询缓存，看是否有对应的秒杀订单
     * @param userId
     * @param goodsId
     * @return
     */
    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {

//        return orderDao.getMiaoshaOrderByUserIdGoodsId(id,goodsId);
        return redisService.get(OrderKey.getMiaoshaOrderByUidGid,""+userId+"_"+goodsId,MiaoshaOrder.class);
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }

    /**
     * 生成订单->写入秒杀订单
     * @param user 用户信息
     * @param goods 商品信息
     * @return orderInfo 订单信息
     */
    @Transactional
    public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
        //暂时将订单id设置为自增，因此在这不需要赋值id @TODO 替换成snowflake
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        //收货地址id，暂时简写
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        //暂时简写1  判断秒杀的渠道：android / ios / pc
        orderInfo.setOrderChannel(1);
        //订单状态：0新建未支付，1待发货，2已发货，3已收货，4已退款，5已完成
        orderInfo.setStatus(NOT_PAID);
        orderInfo.setUserId(user.getId());
        //生成订单,返回1表示插入成功 ，0表示失败
        long flag = orderDao.insert(orderInfo);
        if (flag == 0) return null;

        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(user.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);

        //下单成功后，把秒杀订单放到缓存中
        redisService.set(OrderKey.getMiaoshaOrderByUidGid,""+user.getId()+"_"+goods.getId(),miaoshaOrder);

        return orderInfo;
    }


}
