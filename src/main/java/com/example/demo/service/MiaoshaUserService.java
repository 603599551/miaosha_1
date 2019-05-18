package com.example.demo.service;

import com.example.demo.dao.MiaoshaUserDao;
import com.example.demo.domain.MiaoshaOrder;
import com.example.demo.domain.MiaoshaUser;
import com.example.demo.exception.GlobalException;
import com.example.demo.redis.MiaoshaUserKey;
import com.example.demo.redis.RedisService;
import com.example.demo.result.CodeMsg;
import com.example.demo.util.MD5Util;
import com.example.demo.util.UUIDUtil;
import com.example.demo.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    public static final String COOKIE_NAME_TOKEN="token";

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    /**
     * 根据id获取用户信息
     * @param id
     * @return
     */
    public MiaoshaUser getById(long id){
        //优先取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById,""+id,MiaoshaUser.class);
        if (user != null){
            return user;
        }
        //缓存查不到，就查询数据库
        user = miaoshaUserDao.getById(id);
        //放入缓存，过期时间永久
        if (user != null){
            redisService.set(MiaoshaUserKey.getById,""+id,user);
        }
        return user;
    }

    /**
     * 更改账号的密码
     * @param token
     * @param id
     * @param formPass
     * @return
     */
    public boolean updatePassword(String token, long id, String formPass){
        //取user
        MiaoshaUser user = getById(id);
        if (user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库，修改什么就更新什么，这样可以提高效率
        MiaoshaUser toBeUpdate = new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDbPass(formPass,user.getSalt()));
        miaoshaUserDao.update(toBeUpdate);
        //处理token缓存
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token,token,user);
        //@TODO 为啥不更新，是直接删除???
        redisService.delete(MiaoshaUserKey.getById,""+id);
        return true;
    }

    /**
     * 登录功能
     * @param response
     * @param loginVo
     * @return
     */
    public boolean login(HttpServletResponse response,LoginVo loginVo) {

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
        String calPwd= MD5Util.formPassToDbPass(formPwd,saltDB);
        if(!StringUtils.equals(calPwd,dbPwd)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成TOKEN
        String token= UUIDUtil.uuid();
        //生成cookie
        addCookie(response,token,user);

        return true;
    }

    /**
     * 根据token获取redis缓存中的用户信息（session）
     * 并延长cookie和session的有效期
     * @param response
     * @param token
     * @return
     */
    public MiaoshaUser getByToken(HttpServletResponse response,String token) {
        if(StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
        //延长cookie和session的有效期
        if (user != null){
            addCookie(response,token,user);
        }
        return user;
    }

    /**
     * 客户端的cookie 存储 token
     * 服务端的redis缓存 存储 K-V token-session
     * @param response
     * @param user
     */
    private void addCookie(HttpServletResponse response,String token,MiaoshaUser user){
        //标识token对应着哪个用户，因此需要把token(key)和用户信息(value)写到redis当中
        redisService.set(MiaoshaUserKey.token,token,user);
        //生成cookie
        Cookie cookie=new Cookie(COOKIE_NAME_TOKEN,token);
        //cookie的过期时间与token的一致
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        //设置到网站的根目录
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 生成token
     * @param response
     * @param loginVo
     * @return
     */
    public String createToken(HttpServletResponse response, LoginVo loginVo) {

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
        String calPwd= MD5Util.formPassToDbPass(formPwd,saltDB);
        if(!StringUtils.equals(calPwd,dbPwd)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成TOKEN
        String token= UUIDUtil.uuid();
        //生成cookie
        addCookie(response,token,user);

        return token;
    }
}
