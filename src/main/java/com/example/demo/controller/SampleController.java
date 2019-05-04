package com.example.demo.controller;


import com.example.demo.domain.User;
import com.example.demo.redis.RedisService;
import com.example.demo.redis.UserKey;
import com.example.demo.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

    @Autowired
    RedisService redisService;


    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User  user  = redisService.get(UserKey.getById, ""+1, User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user=new User();
        user.setId(1);
        user.setName("11111");
        boolean flag=redisService.set(UserKey.getById,"1",user);
        return Result.success(flag);
    }
}
