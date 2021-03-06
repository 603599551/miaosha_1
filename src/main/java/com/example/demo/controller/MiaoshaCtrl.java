package com.example.demo.controller;

import com.example.demo.access.AccessLimit;
import com.example.demo.domain.MiaoshaOrder;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.domain.OrderInfo;
import com.example.demo.rabbitmq.MQSender;
import com.example.demo.rabbitmq.MiaoshaMessage;
import com.example.demo.redis.AccessKey;
import com.example.demo.redis.GoodsKey;
import com.example.demo.redis.MiaoshaKey;
import com.example.demo.redis.RedisService;
import com.example.demo.result.CodeMsg;
import com.example.demo.result.Result;
import com.example.demo.service.GoodsService;
import com.example.demo.service.MiaoshaService;
import com.example.demo.service.MiaoshaUserService;
import com.example.demo.service.OrderService;
import com.example.demo.util.MD5Util;
import com.example.demo.util.UUIDUtil;
import com.example.demo.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaCtrl implements InitializingBean{

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

    //key-goodsId ； value- 售罄true 否则false
    private Map<Long,Boolean> localOverMap = new HashMap<>();

    /**
     * 系统初始化，会回调该函数
     * 将商品库存数量加载到Redis缓存中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) return;
        for (GoodsVo goods : goodsList){
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goods.getId(),goods.getStockCount());
            localOverMap.put(goods.getId(),false);
        }
    }

    /**
     * 5000并发 * 10次
     * 优化之前QPS:546
     * 优化之后QPS:2100+
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doMiaosha(Model model, MiaoshaUser user,
                                     @RequestParam("goodsId")long goodsId,
                                     @PathVariable("path")String path){
        model.addAttribute("user",user);
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证秒杀地址path是否正确
        boolean flag = miaoshaService.checkPath(user, goodsId, path);
        if (!flag) return Result.error(CodeMsg.REQUEST_ILLEGAL);

        //map标记，减少对Redis的访问
        boolean isOver = localOverMap.get(goodsId);
        if (isOver) return Result.error(CodeMsg.MIAOSHA_OVER);

        //在Redis中预减库存 -- 减少对数据库的访问
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
        if (stock < 0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }

        //查询Redis缓存，判断之前是否秒杀到了商品，防止重复秒杀 （这一步是为了减少数据库的访问，防止重复秒杀的根本还是要靠数据库的唯一索引）
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order != null){
            return Result.error(CodeMsg.REPEAT_MIAOSHA_ERROR);
        }

        //秒杀请求入队 -- 实现异步下单
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
        //0 表示排队中
        return Result.success(0);

        //接口未优化的过程
//        //判断库存，防止超卖现象 @TODO （目前没有做并发，后期要补上）
//        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//        int stock = goods.getStockCount();
//        if(stock <= 0){
//            return Result.error(CodeMsg.MIAOSHA_OVER);
//        }
//        //查询Redis缓存，判断之前是否秒杀到了商品，防止重复秒杀 （这一步是为了减少数据库的访问，防止重复秒杀的根本还是要靠数据库的唯一索引）
//        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
//        if (order != null){
//            return Result.error(CodeMsg.REPEAT_MIAOSHA_ERROR);
//        }
//
//        //事务：减库存->下订单->写入秒杀订单
//        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
//        return Result.success(orderInfo);
    }

    /**
     * 客户端收到“排队中”响应后，轮询服务器是否秒杀成功
     * @param model
     * @param user
     * @param goodsId
     * @return 秒杀成功，返回orderId；失败就返回-1；排队中就返回0
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser user,
                                     @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //秒杀成功，返回orderId；失败就返回-1；排队中就返回0
        long result = miaoshaService.getMiaoshaResult(user.getId(),goodsId);
        return Result.success(result);
    }


    /**
     * 获取秒杀地址 -- 随机生成的path
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0")int verifyCode) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证验证码的结果是否正确
        boolean flag = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if(!flag) return Result.error(CodeMsg.REQUEST_ILLEGAL);

        String path = miaoshaService.createMiaoshaPath(user, goodsId);
        return Result.success(path);
    }

    /**
     * 生成图片验证码
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, MiaoshaUser user,
                                               @RequestParam("goodsId")long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
            //数据在这里返回了，因此后面是return null
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
