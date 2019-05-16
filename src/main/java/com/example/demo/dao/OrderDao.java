package com.example.demo.dao;

import com.example.demo.domain.MiaoshaOrder;
import com.example.demo.domain.OrderInfo;
import com.example.demo.vo.GoodsVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderDao {

    @Select("SELECT * FROM miaosha_order WHERE user_id=#{userId} AND goods_id=#{goodsId}")
    MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId")long userId,@Param("goodsId") long goodsId);

    /**
     * insert操作，返回值是插入成功的行数
     * 但注解配置@SelectKey后，可以将新插入行的id映射到domain中相应的属性
     * @param orderInfo
     * @return
     */
    @Insert("INSERT INTO order_info (user_id,goods_id,goods_name,goods_count,goods_price,order_channel,status,create_date) " +
            "values (#{userId},#{goodsId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{status},#{createDate})")
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = long.class, before = false, statement = "SELECT last_insert_id()")
    long insert(OrderInfo orderInfo);

    @Insert("INSERT INTO miaosha_order (user_id,goods_id,order_id) values(#{userId},#{goodsId},#{orderId})")
    int insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);
}
