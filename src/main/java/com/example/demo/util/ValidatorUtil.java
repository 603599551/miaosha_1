package com.example.demo.util;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @date 2019-05-07
 * 验证器工具类
 */
public class ValidatorUtil {

    //通过正则表达式来校验手机号 : 开头是1 ，后面是10位数字
    private static final Pattern mobile_pattern=Pattern.compile("1\\d{10}");

    /**
     * 验证手机号格式是否正确
     * @param src
     * @return
     */
    public static boolean isMobile(String src){
        if(StringUtils.isEmpty(src)){
            return false;
        }
        Matcher m=mobile_pattern.matcher(src);
        return m.matches();
    }

}
