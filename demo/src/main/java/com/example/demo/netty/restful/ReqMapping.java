package com.example.demo.netty.restful;

import java.lang.annotation.*;

/**
 * @author bruce - 2018/3/11 12:54
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReqMapping {

    String[] value() default {};

    ReqMethod[] method() default {ReqMethod.GET, ReqMethod.POST};

}
