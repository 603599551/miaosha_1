package com.example.demo.redis;

public class MiaoshaKey extends BasePrefix {


    private MiaoshaKey(String prefix) {
        super(prefix);
    }

    private MiaoshaKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static MiaoshaKey isGoodOver=new MiaoshaKey("go");

}
