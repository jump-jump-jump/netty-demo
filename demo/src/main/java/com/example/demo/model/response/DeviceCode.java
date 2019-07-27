package com.example.demo.model.response;

import com.example.demo.model.response.common.ResultCode;

/**
 * @ClassName DeviceCode
 * @Author zs427
 * @Date 2019-7-17 19:03
 * @Version 1.0
 */
public enum DeviceCode implements ResultCode {

    CONNECTION_EXCEPTION(20008, "连接异常，服务无法找到对端的设备，请重新连接！");

    //操作代码
    int ResponseCode;
    //提示信息
    String ErrorMessage;

    private DeviceCode(int responseCode, String errorMessage) {
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
