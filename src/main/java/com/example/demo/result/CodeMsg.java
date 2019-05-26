package com.example.demo.result;

public class CodeMsg {
    private int code;
    private String msg;

    //通用异常
    public static CodeMsg SUCCESS=new CodeMsg(0,"success");
    public static CodeMsg SERVER_ERROR =new CodeMsg(500100,"server error");
    public static CodeMsg BIND_ERROR =new CodeMsg(500101,"参数校验异常:%s");
    public static CodeMsg REQUEST_ILLEGAL =new CodeMsg(500102,"请求非法");
    public static CodeMsg ACCESS_TIMES_LIMIT =new CodeMsg(500103,"访问太频繁");

    //登录模块 5002XX
    public static CodeMsg SESSION_ERROR=new CodeMsg(500210,"session不存在或已失效");
    public static CodeMsg PASSWORD_EMPTY=new CodeMsg(500211,"登录密码不能为空");
    public static CodeMsg PASSWORD_ERROR=new CodeMsg(500215,"登录密码错误");
    public static CodeMsg MOBILE_EMPTY=new CodeMsg(500212,"手机号不能为空");
    public static CodeMsg MOBILE_ERROR=new CodeMsg(500213,"手机号格式错误");
    public static CodeMsg MOBILE_NOT_EXIST=new CodeMsg(500214,"手机号不存在");

    //商品模块 5003XX

    //订单模块 5004XX
    public static CodeMsg ORDER_NOT_EXIST = new CodeMsg(500400, "订单不存在");

    //秒杀模块 5005XX
    public static CodeMsg MIAOSHA_OVER=new CodeMsg(500500,"商品已经秒杀完毕");
    public static CodeMsg REPEAT_MIAOSHA_ERROR=new CodeMsg(500501,"商品不能重复秒杀");
    public static CodeMsg MIAOSHA_FAIL=new CodeMsg(500502,"商品秒杀失败");

    private CodeMsg(int code,String msg){
        this.code=code;
        this.msg=msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 带参提示，如BIND_ERROR的%s
     * @param args
     * @return
     */
    public CodeMsg fillArgs(Object...args){
        int code = this.code;
        String message = String.format(this.msg,args);
        return new CodeMsg(code,message);
    }

    @Override
    public String toString() {
        return "CodeMsg{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
