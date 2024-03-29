package com.example.demo.netty.restful;

import com.alibaba.fastjson.JSON;
import com.example.demo.constant.HttpRequest;
import com.example.demo.model.response.UserCode;
import com.example.demo.model.response.common.ResponseResult;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.SpringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 *  获取方法参数名两种解决方案:
 *     1 利用java8的新特性Parameter[] parameters = method.getParameters();可以获取,但是要开启-parameters参数
 *     2 利用Spring的LocalVariableTableParameterNameDiscoverer解析方法参数名,好处是不需要开启Java8特性
 *  应该在什么时候解析方法参数名:
 *     1 在方法调用的时候,这样的好处是产生的是局部变量内存回收块,占用资源少
 *     2 在初始化所有的类实例,映射url到方法的时候,通过map保存为全局变量,不需要每次都解析,加快运行速度
 * </pre>
 *
 * @author bruce - 2018/3/11 17:31
 */
public class RestProcessor {

    private static RestProcessor instance;

    public static synchronized RestProcessor initScanPackage(String scanPackage) {
        if (instance == null) {
            instance = new RestProcessor(scanPackage);
        }
        return instance;
    }

    public static RestProcessor getInstance() {
        return instance;
    }

    private ConcurrentHashMap<String, MethodInfo> urlMethodInfoMap = new ConcurrentHashMap<>();

    private RestProcessor(String scanPackage) {
        if (CollectionUtils.isEmpty(urlMethodInfoMap)) {
            prepare(scanPackage);
        }
    }

    /**
     * 扫描控制器注解类,并实例化,解析url和方法映射关系
     */
    private void prepare(String scanPackage) {
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        Set<Class<?>> classesInPackage = new PackageScanUtil().getClassesInPackage(scanPackage, false);
        for (Class<?> aClass : classesInPackage) {
            //System.out.println("扫描到的类: " + aClass.getName());
            if (aClass.isAnnotationPresent(Rest.class)) {
                Object obj = SpringUtil.getBean(aClass);
                Rest restAnnotation = aClass.getAnnotation(Rest.class);
                //防止映射的url没写分割符,拼接出错误的url
                String path1 = "/".concat(restAnnotation.value()).concat("/");
                Method[] declaredMethods = aClass.getDeclaredMethods();
                if (declaredMethods != null) {
                    for (Method method : declaredMethods) {
                        if (Modifier.isPublic(method.getModifiers())) {
                            ReqMapping mapping = method.getAnnotation(ReqMapping.class);
                            if (mapping != null) {
                                String url = null;
                                String[] paths = mapping.value();
                                MethodInfo methodInfo = new MethodInfo(obj, method);
                                methodInfo.supportMethod = mapping.method();
                                methodInfo.parameterNames = discoverer.getParameterNames(methodInfo.method);
                                if (paths.length == 0) { //如果注解路径参数为空,就以方法名作为url路径
                                    String name = method.getName();
                                    url = toReqUrl(path1.concat(name));
                                    this.saveReqMapping(url, methodInfo);
                                } else {
                                    for (String path : paths) {
                                        url = toReqUrl(path1.concat(path));
                                        this.saveReqMapping(url, methodInfo);
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    public void invoke(ChannelHandlerContext ctx, FullHttpRequest request) {
        String queryUri = request.uri(); //获取客户端访问的Uri
        //分割掉get请求携带的参数
        String uri = queryUri;
        if (uri.contains("?")) {
            uri = uri.substring(0, uri.indexOf("?"));
        }
        //判断请求的url是否存在,uri没有映射的方法返回404
        if (!urlMethodInfoMap.containsKey(uri)) {
            HttpUtil.sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        HttpMethod method = request.method(); //获取客户端请求方法
        MethodInfo methodInfo = urlMethodInfoMap.get(uri);//通过url获取映射请求的方法
        boolean supportMethod = methodInfo.isSupportMethod(method);
        if (!supportMethod) { //不支持的请求方法
            HttpUtil.sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        //解析请求参数
        Map<String, List<String>> queryParams = new HashMap<>();
        if (method == HttpMethod.GET) {
            //解析get请求参数
            queryParams.putAll(decodeGetParams(queryUri));
        } else if (request.method() == HttpMethod.POST) {
            // 处理POST请求
            queryParams.putAll(decodeGetParams(queryUri));
            queryParams.putAll(decodePostParams(request));
        }
        //解析方法参数名
        String[] parameterNames = methodInfo.parameterNames;
        //获取参数类型
        Class<?>[] parameterTypes = methodInfo.method.getParameterTypes();
        //获取参数对应的注解
        Annotation[][] parameterAnnotations = methodInfo.method.getParameterAnnotations();

        // 查看是否需要校验 AccessToken
        ValidateAccessToken ifCheckToken = methodInfo.method.getAnnotation(ValidateAccessToken.class);
        if (null != ifCheckToken) {
            CharSequence accessTokenCs = request.headers().get(ifCheckToken.name());
            if (StringUtils.isEmpty(accessTokenCs)
                    || !HttpRequest.HTTP_ACCESS_TOKEN_LIST.contains(accessTokenCs.toString())) {
                HttpUtil.sendResp(ctx, new ResponseResult(UserCode.ACCESS_TOKEN_ERROR), request);
                return;
            }
        }

        try {
            MethodInvokeArgs result = parseMethodInvokeArgs(parameterNames, parameterTypes, parameterAnnotations, queryParams);
            if (result.status == HttpResponseStatus.BAD_REQUEST) {
                HttpUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            Object rtn;
            if (parameterTypes.length > 0) {
                rtn = methodInfo.method.invoke(methodInfo.obj, result.methodInvokeArgs);
            } else {
                rtn = methodInfo.method.invoke(methodInfo.obj);
            }
            HttpUtil.sendResp(ctx, rtn, request);
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 解析get的请求参数
     *
     * @param queryUri 浏览器输入的url
     */
    private Map<String, List<String>> decodeGetParams(String queryUri) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(queryUri);
        return queryStringDecoder.parameters();
    }

    /**
     * 解析post请求参数
     */
    private Map decodePostParams(FullHttpRequest request) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
                new DefaultHttpDataFactory(false), request);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas(); //
        if (postData.size() == 0) {
            return Collections.emptyMap();
        }
        Map params = new LinkedHashMap<>(postData.size());
        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                JSON.parseObject(data.toString(), Map.class).forEach((key, value) -> {
                    List<Object> list = new ArrayList<>();
                    list.add(value);
                    params.put(key, list);
                });
            }
        }
        return params;
    }

    /**
     * 将扫描注解得到的url和将对用的对象,方法映射
     *
     * @param url        扫描拼接得到的url
     * @param methodInfo 对应控制器实例的实例方法
     */
    private void saveReqMapping(String url, MethodInfo methodInfo) {
        if (urlMethodInfoMap.containsKey(url)) {
            throw new RuntimeException("存在多个请求映射:".concat(url));
        }
        urlMethodInfoMap.put(url, methodInfo);
        System.out.println(url + " methodInfo:" + methodInfo.hashCode()
                + " obj:" + methodInfo.obj.hashCode() + " "
                + JSON.toJSONString(methodInfo.supportMethod));
    }

    /**
     * <pre>
     * 拼接出url可能出现多个分割符的情况,重新分割拼接
     * 例如:/mvnrepository.com///artifact//javax/javaee-api/8.0
     * 调整后为:/mvnrepository.com/artifact/javax/javaee-api/8.0
     * </pre>
     */
    private String toReqUrl(String url) {
        String[] split = url.split("/");
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            if (!StringUtils.isEmpty(s)) {
                builder.append("/").append(s);
            }
        }
        return builder.toString();
    }

    /**
     * 将http请求参数转为控制器方法调用参数
     *
     * @param parameterNames 控制器方法参数名
     * @param parameterTypes 控制器方法参数类型
     * @param httpParams     请求参数
     * @return
     */
    private MethodInvokeArgs parseMethodInvokeArgs(String[] parameterNames, Class<?>[] parameterTypes, Annotation[][] parameterAnnotations, Map<String, List<String>> httpParams) throws IllegalAccessException, InstantiationException {
        MethodInvokeArgs result = new MethodInvokeArgs();
        result.status = HttpResponseStatus.CONTINUE;
        Object[] args = null;
        if (parameterNames != null && parameterNames.length > 0) {
            args = new Object[parameterNames.length];
            for (int i = 0; i < parameterNames.length; i++) {
                //http请求参数对应的值
                List<String> values = null;
                Object parameterObj = null;
                ReqParam paramAnno = getReqParamAnnotation(parameterAnnotations, i);
                //处理该参数无@ReqParam注解的情况
                if (paramAnno == null) {
                    values = httpParams.get(parameterNames[i]);
                    if (null == values) {
                        // 自定义类型参数对象
                        Class<?> parameterClass = parameterTypes[i];
                        for (Class<?> clazz = parameterClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
                            Field[] fields = clazz.getDeclaredFields();
                            for (Field field : fields) {
                                if (httpParams.containsKey(field.getName())) {
                                    if (null == parameterObj) {
                                        parameterObj = parameterClass.newInstance();
                                    }
                                    field.setAccessible(true);
                                    try {
                                        String name = field.getName();
                                        values = httpParams.get(name);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Object paramValue = handlerParameter(values, field.getType());
                                    field.set(parameterObj, paramValue);
                                }
                            }
                        }
                    }
                    if (null == parameterObj && null == values) {
                        //返回客户端错误的请求信息
                        result.status = HttpResponseStatus.BAD_REQUEST;
                        return result;
                    }
                } else {
                    //处理该参数有@ReqParam注解的情况
                    // 如果注解的value不为空则使用注解的value作为http请求参数名,否则使用方法参数名
                    String paramName = !"".equals(paramAnno.value().trim()) ? paramAnno.value().trim() : parameterNames[i];
                    values = httpParams.get(paramName);
                    //如果该参数要求必填,且参数值为null,返回客户端错误的请求信息
                    if (paramAnno.required() && values == null) {
                        result.status = HttpResponseStatus.BAD_REQUEST;
                        return result;
                    }
                }
                //注意这个时候得到的 http 请求值values可能为null或者size = 0,
                //size=0表示客户端传递了参数名但没有值
                Class<?> parameterType = parameterTypes[i];
                if (null == parameterObj) {
                    args[i] = handlerParameter(values, parameterType);
                } else {
                    args[i] = parameterObj;
                }
            }
        }
        result.methodInvokeArgs = args;
        return result;
    }

    private Object handlerParameter(List<String> values, Class<?> parameterClass) {
        Object paramValue = null;
        if (String.class == parameterClass) {
            if (values.size() == 0) {
                paramValue = "";
            } else {
                paramValue = values.get(0);
            }
        } else if (int.class == parameterClass) {
            paramValue = values == null || values.size() == 0 ? 0 : Integer.parseInt(values.get(0));
        } else if (Integer.class == parameterClass) {
            paramValue = values == null || values.size() == 0 ? null : Integer.valueOf(values.get(0));
        } else if (long.class == parameterClass) {
            paramValue = values == null || values.size() == 0 ? 0 : Long.parseLong(values.get(0));
        } else if (Long.class == parameterClass) {
            paramValue = values == null || values.size() == 0 ? null : Long.valueOf(values.get(0));
        } else if (String[].class == parameterClass) {
            paramValue = values == null || values.size() == 0 ? null : values.toArray(new String[0]);
        } else if (List.class == parameterClass || ArrayList.class == parameterClass) {
            paramValue = values;
        }
        return paramValue;
    }

    public ReqParam getReqParamAnnotation(Annotation[][] annotations, int index) {
        ReqParam reqParam = null;
        Annotation[] annotation = annotations[index];
        if (annotation != null && annotation.length > 0) {
            reqParam = (ReqParam) Arrays.stream(annotation)
                    .filter(anno -> anno.annotationType() == ReqParam.class)
                    .findFirst().orElse(null);
        }
        return reqParam;
    }

    /**
     * 方法调用参数,如果status为HttpResponseStatus.BAD_REQUEST;提示客户端请求错误信息
     */
    private class MethodInvokeArgs {
        private Object[] methodInvokeArgs;
        private HttpResponseStatus status;
    }

    /**
     * url映射的方法信息
     */
    private class MethodInfo {
        /**
         * @param obj    控制器实例对象
         * @param method 请求对应的方法
         */
        private MethodInfo(Object obj, Method method) {
            this.obj = obj;
            this.method = method;
        }

        private Object obj;    //method所在实体类
        private Method method;
        /**
         * 该方法支持的http请求方式
         */
        private ReqMethod[] supportMethod;
        /**
         * 方法参数名
         */
        private String[] parameterNames;

        private boolean isSupportMethod(HttpMethod reqMethod) {
            return supportMethod != null && supportMethod.length > 0
                    && Arrays.stream(supportMethod).anyMatch(support -> support.name().equalsIgnoreCase(reqMethod.name().toString()));
        }
    }

}
