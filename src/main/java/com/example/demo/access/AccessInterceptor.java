package com.example.demo.access;

import com.alibaba.fastjson.JSON;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.redis.AccessKey;
import com.example.demo.redis.RedisService;
import com.example.demo.result.CodeMsg;
import com.example.demo.result.Result;
import com.example.demo.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * 拦截器
 * 作用-- 1.访问限流防刷 2.验证登录
 * 拦截对象： 注解@AccessLimit
 * @date 2019-05-26
 */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter{

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod){
            //验证登录，获取用户对象
            MiaoshaUser user = getUser(request,response);
            //将user对象保存在每个线程的本地变量中
            UserContext.setUser(user);

            //获得注解的参数信息
            HandlerMethod hm = (HandlerMethod)handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) return true;
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if (needLogin){
                if (user == null){
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" +user.getId();
            }
            //@TODO 关于重构代码的经典书籍：martine flower
            //重构-改善既有代码的设计
            AccessKey accessKey = AccessKey.withExpire(seconds);
            //查询访问次数
            Integer count = redisService.get(accessKey,key, Integer.class);
            //第一次访问
            if (count == null)
                redisService.set(accessKey, key, 1);
                //访问次数< maxCount ++
            else if (count < maxCount)
                redisService.incr(accessKey, key);
            else{
                //一分钟之内，访问超过maxCount -- error
                render(response, CodeMsg.ACCESS_TIMES_LIMIT);
                return false;
            }
        }
        return true;
    }

    /**
     * 向客户端发出异常响应
     * @param response
     * @param cm
     * @throws Exception
     */
    private void render(HttpServletResponse response, CodeMsg cm)throws Exception{
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    /**
     * 解析登录请求的参数得到Token
     * 根据Token取到user对象
     * @param request
     * @param response
     * @return
     */
    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){
        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,MiaoshaUserService.COOKIE_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return null;
        }

        String token = StringUtils.isEmpty(paramToken)? cookieToken:paramToken;
        return userService.getByToken(response,token);
    }

    /**
     * 获取request-cookies中的token
     * @param request
     * @param cookieNameToken
     * @return
     */
    private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null || cookies.length == 0){
            return null;
        }
        for (Cookie cookie : cookies){
            if(cookie.getName().equals(cookieNameToken)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
