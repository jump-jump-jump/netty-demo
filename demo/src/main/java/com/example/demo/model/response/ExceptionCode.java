package com.example.demo.model.response;

import com.example.demo.model.response.common.ResultCode;

/**
 * @ClassName IotCode
 * @Author zs427
 * @Date 2019-7-17 18:53
 * @Version 1.0
 */
public enum ExceptionCode implements ResultCode {

    PARAMS_FORMAT_ERROR(90001, "参数格式错误！");

    //操作代码
    int ResponseCode;
    //提示信息
    String ErrorMessage;

    private ExceptionCode(int responseCode, String errorMessage) {
        this.ResponseCode = responseCode;
        this.ErrorMessage = errorMessage;
    }

    @Override
    public int ResponseCode() {
        return ResponseCode;
    }

    @Override
    public String ErrorMessage() {
        return ErrorMessage;
    }

}
