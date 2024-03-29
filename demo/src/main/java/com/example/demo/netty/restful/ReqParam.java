package com.example.demo.netty.restful;

import java.lang.annotation.*;

/**
 * @author bruce - 2018/3/12 14:01
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReqParam {

    /**
     * parameter name
     */
    String value() default "";

    /**
     * Whether the parameter is required.
     * <p>Defaults to {@code true}, leading to an exception being thrown
     * if the parameter is missing in the request. Switch this to
     * {@code false} if you prefer a {@code null} value if the parameter is
     * not present in the request.
     * <p>Alternatively, provide a default value, which implicitly
     * sets this flag to {@code false}.
     */
    boolean required() default true;


}
