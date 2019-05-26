package com.example.demo.service;

import com.example.demo.dao.OrderDao;
import com.example.demo.domain.Goods;
import com.example.demo.domain.MiaoshaOrder;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.domain.OrderInfo;
import com.example.demo.redis.MiaoshaKey;
import com.example.demo.redis.RedisService;
import com.example.demo.util.MD5Util;
import com.example.demo.util.UUIDUtil;
import com.example.demo.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;


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

    /**
     * 查询redis缓存，验证用户的秒杀地址是否正确
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
        if (user == null || path == null) return false;
        String oldPath = redisService.get(MiaoshaKey.getMiaoshaPath,""+user.getId() + "_" + goodsId, String.class);
        return path.equals(oldPath);
    }

    /**
     * 随机生成秒杀地址path，存到redis缓存
     * @param user
     * @param goodsId
     * @return
     */
    public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
        if (user == null || goodsId <= 0) return null;
        String str = MD5Util.md5(UUIDUtil.uuid()+"123456");
        redisService.set(MiaoshaKey.getMiaoshaPath,""+user.getId() + "_" + goodsId, str);
        return str;
    }

    /**
     * 生成图片验证码
     * @param user
     * @param goodsId
     * @return
     */
    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
        if (user == null || goodsId <= 0) return null;
        int width = 80;
        int height = 32;
        //生成缓存图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // 设置背景颜色
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // 画边界
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    /**
     * ScriptEngine 动态执行JS
     * 计算算术表达式的结果
     * @param exp
     * @return
     */
    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            Integer catch1 = (Integer)engine.eval(exp);
            return catch1.intValue();
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //运算符
    private static char[] ops = new char[] {'+', '-', '*'};

    /**
     * 随机生成算式
     * @param rdm
     * @return
     */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    /**
     * 验证验证码的结果是否正确
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) return false;
        Integer oldCode = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
        if (oldCode == null || oldCode - verifyCode != 0) return false;
        //及时删除验证码的结果
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
        return true;
    }
}
