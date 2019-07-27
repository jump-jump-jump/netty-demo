package com.example.demo.exception;

import com.example.demo.model.response.common.ResultCode;

/**
 * @ClassName CustomException
 * @Author zs427
 * @Date 2019-7-19 13:52
 * @Version 1.0
 */
public class CustomException extends RuntimeException {

    //错误代码
    ResultCode resultCode;

    public CustomException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

}
