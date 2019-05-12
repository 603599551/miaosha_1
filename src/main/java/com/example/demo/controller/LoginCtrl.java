package com.example.demo.controller;

import com.example.demo.result.CodeMsg;
import com.example.demo.result.Result;
import com.example.demo.service.MiaoshaUserService;
import com.example.demo.util.ValidatorUtil;
import com.example.demo.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


/**
 * @date 2019-05-07
 */
@Controller
@RequestMapping("/login")
public class LoginCtrl {

    //打印日志
    private static Logger log= LoggerFactory.getLogger(LoginCtrl.class);

    @Autowired
    MiaoshaUserService userService;

    /**
     * 跳转到登录页
     * @return
     */
    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    /**
     * 登录功能
     * @param response
     * @param loginVo
     * @return
     */
    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response,@Valid LoginVo loginVo){
        //日志打印
        log.info(loginVo.toString());
        //登录
        userService.login(response,loginVo);
        return Result.success(true);
    }
}
