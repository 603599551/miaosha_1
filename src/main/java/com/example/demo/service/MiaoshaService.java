package com.example.demo.service;

import com.example.demo.dao.OrderDao;
import com.example.demo.domain.Goods;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.domain.OrderInfo;
import com.example.demo.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @date 2019-05-16
 */
@Service
public class MiaoshaService {


    //不提倡引入别的Dao，要引入对应的Dao。
    //如果有需要，就引入别的Dao对应的Service

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    /**
     * 减库存->下订单->写入秒杀订单
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        //减库存 @TODO 为什么减库存失败时不直接return，而是等到写入秒杀订单的时候通过唯一性来判断是否成功？
        goodsService.reduceStock(goods);
        //下订单->写入秒杀订单
        return orderService.createOrder(user,goods);
    }
}
