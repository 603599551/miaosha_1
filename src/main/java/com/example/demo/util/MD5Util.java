package com.example.demo.util;


import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    public static String md5(String str){
        return DigestUtils.md5Hex(str);
    }

    //固定salt，“拼装”用户端输入的明文密码，整体再做md5
    private static final String salt="1a2b3c4d";

    /**
     * 第一次MD5 用户端：PASSWORD1=MD5(密码明文+固定salt)
     * @param inputPwd
     * @return
     */
    public static String inputPassToFormPass(String inputPwd){
        String str= ""+salt.charAt(0)+salt.charAt(2) + inputPwd +salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     * 第二次MD5 PASSWORD2=MD5(PASSWORD1+随机salt)
     * 然后将PASSWORD2和随机salt存入数据库
     * @param formPwd
     * @param salt
     * @return
     */
    public static String formPassToDbPass(String formPwd, String salt){
        String str= ""+salt.charAt(0)+salt.charAt(2) + formPwd +salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     * 两次MD5
     * @param inputPwd
     * @param saltDB
     * @return
     */
    public static String inputPassToDbPass(String inputPwd, String saltDB){
        String formPwd= inputPassToFormPass(inputPwd);
        String dbPwd= formPassToDbPass(formPwd,saltDB);
        return dbPwd;
    }

    public static void main(String []args){
//        System.out.println(inputPassToFormPass("123456"));
//        System.out.println(formPassToDbPass("d3b1294a61a07da9b49b6e22b2cbd7f9","1a2b3c4d"));
//        System.out.println(inputPassToDbPass("123456","1a2b3c4d"));
    }
}
