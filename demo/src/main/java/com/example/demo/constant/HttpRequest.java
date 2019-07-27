package com.example.demo.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @ClassName HttpRequest
 * @Author zs427
 * @Date 2019-7-16 20:02
 * @Version 1.0
 */
@Configuration
public class HttpRequest {

    // 访问本平台Token
    public static List<String> HTTP_ACCESS_TOKEN_LIST;

    @Value("${http.token}")
    public void setHttpAccessToken(List<String> httpAccessTokenList) {
        HTTP_ACCESS_TOKEN_LIST = httpAccessTokenList;
    }

}
