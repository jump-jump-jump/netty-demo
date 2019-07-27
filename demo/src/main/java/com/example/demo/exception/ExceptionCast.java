package com.example.demo.exception;

import com.example.demo.model.response.ExceptionCode;
import com.example.demo.model.response.common.ResultCode;
import com.google.common.collect.ImmutableMap;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-09-14 17:31
 **/
public class ExceptionCast {

    /**
     * 定义map，配置异常类型所对应的错误代码
     */
    public static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTIONS;

    /**
     * 定义map的builder对象，去构建ImmutableMap
     */
    public static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> builder = ImmutableMap.builder();

    static {
        //定义异常类型所对应的错误代码
        builder.put(ArrayIndexOutOfBoundsException.class, ExceptionCode.PARAMS_FORMAT_ERROR);
    }

    public static void cast(ResultCode resultCode) {
        throw new CustomException(resultCode);
    }

}
