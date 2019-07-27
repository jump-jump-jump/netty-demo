package com.example.demo.controller;

import com.example.demo.model.dto.UserDTO;
import com.example.demo.model.response.common.CommonCode;
import com.example.demo.model.response.common.CommonResponseResult;
import com.example.demo.model.response.common.ResponseResult;
import com.example.demo.netty.restful.*;
import org.springframework.stereotype.Controller;

/**
 * @ClassName DeviceController
 * @Author zs427
 * @Date 2019-7-21 6:40
 * @Version 1.0
 */
@Controller
@Rest("/user")
public class UserController {

    @ValidateAccessToken
    @ReqMapping(value = "/test1", method = ReqMethod.GET)
    public ResponseResult test1(@ReqParam(required = false) UserDTO userDTO) {
        return new CommonResponseResult<>(CommonCode.SUCCESS, userDTO);
    }

}
