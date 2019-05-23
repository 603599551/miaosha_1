package com.example.demo.service;

import com.example.demo.dao.OrderDao;
import com.example.demo.domain.Goods;
import com.example.demo.domain.MiaoshaOrder;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.domain.OrderInfo;
import com.example.demo.redis.MiaoshaKey;
import com.example.demo.redis.RedisService;
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

    @Autowired
    RedisService redisService;


    /**
     * 减库存->下订单->写入秒杀订单
     *
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        //减库存
        boolean flag = goodsService.reduceStock(goods);
        //减库存失败 -> 做个标记，Return
        if (!flag) {
            setGoodsOver(goods.getId());
            return null;
        }
        //下订单->写入秒杀订单
        return orderService.createOrder(user, goods);
    }

    /**
     * 查询Redis缓存中是否有相应的秒杀订单即可
     * @param userId
     * @param goodsId
     * @return
     */
    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId,goodsId);
        //Redis缓存有，说明秒杀成功
        if (order != null){
            return order.getOrderId();
        }else{
            //没有的话，查询Redis内存来判断商品是否卖完了，卖完了就是秒杀失败，否则是排队中
            boolean isOver = getGoodsOver(goodsId);
            return isOver ? -1 : 0;
        }
    }

    /**
     * 商品售罄，在Redis内存中标记 该商品isGoodOver -- true
     * @param goodsId
     */
    public void setGoodsOver(Long goodsId) {
        redisService.set(MiaoshaKey.isGoodOver,""+goodsId,true);
    }

    /**
     * 判断Redis内存中是否存在相应的key
     * @param goodsId
     * @return
     */
    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodOver,""+goodsId);
    }
}
