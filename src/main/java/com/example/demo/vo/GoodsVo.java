package com.example.demo.vo;

import com.example.demo.domain.Goods;

import java.util.Date;

/**
 * @date 2019-05-13
 * 数据对象类：将商品表和秒杀商品表的数据合到一起
 */
public class GoodsVo extends Goods {

    private Double miaoshaPrice;
    //可秒杀的商品库存
    private Integer stockCount;
    private Date startDate;
    private Date endDate;

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getMiaoshaPrice() {
        return miaoshaPrice;
    }

    public void setMiaoshaPrice(Double miaoshaPrice) {
        this.miaoshaPrice = miaoshaPrice;
    }

}
