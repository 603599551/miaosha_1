package com.example.demo.redis;

public interface KeyPrefix {

    int expireSeconds();
    String getPrefix();
}
