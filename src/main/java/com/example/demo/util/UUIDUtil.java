package com.example.demo.util;

import java.util.UUID;

/**
 * @date 2019-05-11
 */
public class UUIDUtil {
    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
