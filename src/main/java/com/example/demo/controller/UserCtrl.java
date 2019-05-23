package com.example.demo.controller;

import com.example.demo.domain.MiaoshaOrder;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.domain.OrderInfo;
import com.example.demo.rabbitmq.MQSender;
import com.example.demo.redis.RedisService;
import com.example.demo.result.CodeMsg;
import com.example.demo.result.Result;
import com.example.demo.service.GoodsService;
import com.example.demo.service.MiaoshaService;
import com.example.demo.service.MiaoshaUserService;
import com.example.demo.service.OrderService;
import com.example.demo.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/mq")
public class UserCtrl {

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

    @RequestMapping("/direct")
    @ResponseBody
    public Result<String> info(){
        sender.sendDirect("hello");
        return Result.success("hello world");
    }

    @RequestMapping("/topic")
    @ResponseBody
    public Result<String> topic(){
        sender.sendTopic("hello,topic");
        return Result.success("hello topic");
    }

    @RequestMapping("/fanout")
    @ResponseBody
    public Result<String> fanout(){
        sender.sendFanout("hello,fanout");
        return Result.success("hello fanout");
    }

    @RequestMapping("/headers")
    @ResponseBody
    public Result<String> headers(){
        sender.sendHeaders("hello,headers");
        return Result.success("hello headers");
    }
}
