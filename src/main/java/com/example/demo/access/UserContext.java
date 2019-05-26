package com.example.demo.access;

import com.example.demo.domain.MiaoshaUser;

/**
 * 把user对象保存到每个线程中
 * @date 2019-05-26
 */
public class UserContext {

    private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<>();

    public static void setUser(MiaoshaUser user){
        userHolder.set(user);
    }

    public static MiaoshaUser getUser(){
        return userHolder.get();
    }
}
