package com.example.demo.access;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义注解配置--访问限流
 * @date 2019-05-26
 * @TODO 详解自定义注解的原理
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit{

    int seconds();

    int maxCount();

    boolean needLogin() default true;

}
