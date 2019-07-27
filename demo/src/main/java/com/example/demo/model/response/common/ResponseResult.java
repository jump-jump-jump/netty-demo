package com.example.demo.model.response.common;

import lombok.*;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult implements Response {

    //操作代码
    int ResponseCode = SUCCESS_CODE;

    //提示信息
    String ErrorMessage;

    long currentTime = CURRENT_TIME;

    public ResponseResult(ResultCode resultCode) {
        this.ResponseCode = resultCode.ResponseCode();
        this.ErrorMessage = resultCode.ErrorMessage();
    }

    public static ResponseResult SUCCESS() {
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public static ResponseResult FAIL() {
        return new ResponseResult(CommonCode.FAIL);
    }

}
