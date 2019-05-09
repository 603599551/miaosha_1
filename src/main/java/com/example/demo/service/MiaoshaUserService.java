package com.example.demo.service;

import com.example.demo.dao.MiaoshaUserDao;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.exception.GlobalException;
import com.example.demo.result.CodeMsg;
import com.example.demo.util.MD5Util;
import com.example.demo.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

@Service
public class MiaoshaUserService {

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    public MiaoshaUser getById(long id){
        return miaoshaUserDao.getById(id);
    }

    public boolean login(LoginVo loginVo) {

        if(loginVo==null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        String mobile=loginVo.getMobile();
        String formPwd=loginVo.getPassword();

        //判断手机号是否存在
        MiaoshaUser user=getById(Long.parseLong(mobile));
        if(user==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPwd=user.getPassword();
        String saltDB=user.getSalt();
        String calPwd= MD5Util.formPwdToDBPwd(formPwd,saltDB);
        if(!StringUtils.equals(calPwd,dbPwd)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        return true;
    }
}
