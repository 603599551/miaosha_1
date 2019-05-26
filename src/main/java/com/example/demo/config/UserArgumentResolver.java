package com.example.demo.config;

import com.example.demo.access.UserContext;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.service.MiaoshaUserService;
import com.sun.istack.internal.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
//import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @date 2019-05-12
 * 解析User对象
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    MiaoshaUserService userService;

    /**
     * 判断参数类型是否是User类型
     * @param methodParameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        //获取参数的类型
        Class<?> clazz = methodParameter.getParameterType();
        return clazz == MiaoshaUser.class;
    }

    /**
     * 调用该方法前，拦截器已经验证登录，将user对象保存到每个线程的本地变量中，因此可以直接获取。
     * @param methodParameter
     * @param modelAndViewContainer
     * @param nativeWebRequest
     * @param webDataBinderFactory
     * @return
     * @throws Exception
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return UserContext.getUser();
    }

//    /**
//     * 获取request-cookies中的token
//     * @param request
//     * @param cookieNameToken
//     * @return
//     */
//    private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
//        Cookie[] cookies = request.getCookies();
//        if(cookies == null || cookies.length == 0){
//            return null;
//        }
//        for (Cookie cookie : cookies){
//            if(cookie.getName().equals(cookieNameToken)){
//                return cookie.getValue();
//            }
//        }
//        return null;
//    }
}
