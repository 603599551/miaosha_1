package com.example.demo.result;

public class Result<T> {
    private int code;
    private String msg;
    private T data;


    private Result(T data){
        this.code=0;
        this.msg="success";
        this.data=data;
    }

    public Result(CodeMsg cm) {
        if (cm==null) return;
        this.code=cm.getCode();
        this.msg=cm.getMsg();
    }

    /**
     * 成功时的调用 testGit
     * @param data
     * @param <T>
     * @return
     */
    public static<T> Result<T> success(T data){ //static<T> 将该方法声明为泛型方法，才能将泛型T作为方法的返回值
        return new Result<T>(data);
    }

    public static<T> Result<T> error(CodeMsg cm){
        return new Result<T>(cm);
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
