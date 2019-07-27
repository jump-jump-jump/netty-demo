package com.example.demo.model.response.common;

/**
 * 返回结果代码
 */
public interface ResultCode {

    /**
     * 操作代码
     */
    int ResponseCode();

    /**
     * 提示信息
     */
    String ErrorMessage();

}
