package com.example.demo.util;

import java.util.UUID;

/**
 * @date 2019-05-11
 */
public class UUIDUtil {
    //@TODO  替换成snowflake算法 -- 分布式ID
    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
