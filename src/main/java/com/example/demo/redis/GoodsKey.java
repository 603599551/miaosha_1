package com.example.demo.redis;

public class GoodsKey extends BasePrefix {


    private GoodsKey(String prefix) {
        super(prefix);
    }

    private GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    //页面缓存是为了防止瞬间的访问量导致服务器压力太大，但是为了保证信息的实时性又不能缓存太长时间。
    public static GoodsKey getGoodsList=new GoodsKey(60,"gl");
    public static GoodsKey getGoodsDetail=new GoodsKey(60,"gd");
    public static GoodsKey getMiaoshaGoodsStock=new GoodsKey(0,"gs");

}
