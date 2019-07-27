package com.example.demo.model.response.common;

import lombok.ToString;

@ToString
public enum CommonCode implements ResultCode {
    INVALID_PARAM(400, "非法参数！"),
    SUCCESS(200, "操作成功！"),
    FAIL(300, "操作失败！"),
    NON_RESOURCES(400, "资源不存在！"),
    SERVER_ERROR(500, "抱歉，系统繁忙，请稍后重试！");
    //操作代码
    int ResponseCode;
    //提示信息
    String ErrorMessage;

    private CommonCode(int responseCode, String errorMessage) {
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
