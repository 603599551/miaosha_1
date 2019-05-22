package com.example.demo.vo;

import com.example.demo.domain.OrderInfo;

/**
 * @date 2019-05-21
 */
public class OrderDetailVo {
    private GoodsVo goodsVo;
    private OrderInfo order;

    public GoodsVo getGoodsVo() {
        return goodsVo;
    }

    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }

    public OrderInfo getOrderInfo() {
        return order;
    }

    public void setOrderInfo(OrderInfo order) {
        this.order = order;
    }
}
