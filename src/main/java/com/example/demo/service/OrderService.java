package com.example.demo.service;

import com.example.demo.dao.GoodsDao;
import com.example.demo.dao.OrderDao;
import com.example.demo.domain.MiaoshaOrder;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.domain.OrderInfo;
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


    @Autowired
    OrderDao orderDao;


    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long id, long goodsId) {

        return orderDao.getMiaoshaOrderByUserIdGoodsId(id,goodsId);
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
        //暂时简写1 @TODO 秒杀的渠道：android / ios / pc ? 如何判断??
        orderInfo.setOrderChannel(1);
        //订单状态：0新建未支付，1待发货，2已发货，3已收货，4已退款，5已完成 @TODO 用枚举类型表示一下
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        //生成订单,返回1表示插入成功 ，0表示失败
        long orderId = orderDao.insert(orderInfo);
        if (orderId == 1){
            orderId = orderInfo.getId();
        }else{
            return null;
        }

        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderId);
        miaoshaOrder.setUserId(user.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);

        return orderInfo;
    }
}
