package com.example.demo.aspect;

import com.alibaba.fastjson.JSON;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ExceptionCast;
import com.example.demo.model.response.common.Response;
import com.example.demo.model.response.common.ResponseResult;
import com.example.demo.model.response.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Arrays;

@Slf4j
@Component
@Aspect
@Order(0)
public class LogExceptionAspect {

    @Pointcut("execution(public * com.example.demo.controller.*.*(..))")
    public void logExceptionPointCut() {
    }

    @Around(value = "logExceptionPointCut()||@annotation(LogExceptionPointCut)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = null;
        StringBuilder sb = new StringBuilder("\n");
        try {
            sb.append(String.format("类名：%s\n", proceedingJoinPoint.getTarget().getClass().getName()));

            MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
            sb.append(String.format("方法：%s\n", signature.getMethod().getName()));

            Object[] args = proceedingJoinPoint.getArgs();
            Arrays.stream(args).forEach(arg -> {
                if (arg instanceof BindingResult) {
                    return;
                }
                sb.append(String.format("参数：%s\n", JSON.toJSON(arg)));
            });

            long startTime = System.currentTimeMillis();
            result = proceedingJoinPoint.proceed(args);
            long endTime = System.currentTimeMillis();
            sb.append(String.format("返回：%s\n", JSON.toJSON(result)));
            sb.append(String.format("耗时：%sms", endTime - startTime));
        } catch (CustomException e) {
            sb.append(String.format("异常：%s", e.getResultCode()));
            ResultCode resultCode = e.getResultCode();
            return new ResponseResult(resultCode);
        } catch (Exception e) {
            sb.append(String.format("异常：%s", e.getMessage()));
            if (ExceptionCast.EXCEPTIONS == null) {
                // EXCEPTIONS构建成功
                ExceptionCast.EXCEPTIONS = ExceptionCast.builder.build();
            }
            //从EXCEPTIONS中找异常类型所对应的错误代码，如果找到了将错误代码响应给用户，如果找不到给用户响应99999异常
            ResultCode resultCode = ExceptionCast.EXCEPTIONS.get(e.getClass());
            if (resultCode != null) {
                return new ResponseResult(resultCode);
            } else {
                // 返回99999异常
                return new ResponseResult(Response.ERROR_CODE, e.getMessage(), Response.CURRENT_TIME);
            }
        } finally {
            log.info(sb.toString());
        }
        return result;
    }

}
