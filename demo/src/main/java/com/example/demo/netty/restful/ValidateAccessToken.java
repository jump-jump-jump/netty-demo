package com.example.demo.netty.restful;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName CheckoutHttpRequestAccessTokenPointCut
 * @Author zs427
 * @Date 2019-7-19 9:14
 * @Version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateAccessToken {

    /**
     * header 中的 key
     * cn.smartclass.nettyiot.constant.HttpRequest.HTTP_ACCESS_TOKEN_LIST 存放 Token 集合
     *
     * @return
     */
    String name() default "AccessToken";

}
