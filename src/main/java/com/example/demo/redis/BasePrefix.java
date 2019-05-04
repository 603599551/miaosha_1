package com.example.demo.redis;

public abstract class BasePrefix implements KeyPrefix {

    //过期时间
    private int expireSeconds;

    private String prefix;

    public BasePrefix(String prefix) {//默认0代表永不过期
        this(0, prefix);
    }

    public BasePrefix(int expireSeconds,String prefix){
        this.expireSeconds=expireSeconds;
        this.prefix=prefix;
    }

    @Override
    public int expireSeconds() {//默认0代表永不过期
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        String className=this.getClass().getSimpleName();
        return className+":"+prefix;
    }
}
