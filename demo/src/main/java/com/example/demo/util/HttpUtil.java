package com.example.demo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

/**
 * @ClassName HttpUtil
 * @Author zs427
 * @Date 2019-7-17 9:50
 * @Version 1.0
 */
public class HttpUtil {

    /**
     * 解析路径获取uri
     *
     * @param fullHttpRequest
     * @return
     */
    public static String getAccessUriFormChannel(FullHttpRequest fullHttpRequest) {
        QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
        return decoder.path();
    }

    /**
     * 获取GET方式传递的参数
     *
     * @param fullHttpRequest
     * @return
     */
    public static Map<String, Object> getPathParamsFromChannel(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<String, Object>(16);
        // 处理get请求
        QueryStringDecoder decoder = new QueryStringDecoder(fullHttpRequest.uri());
        Map<String, List<String>> paramList = decoder.parameters();
        for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
            params.put(entry.getKey(), entry.getValue().get(0));
        }
        return params;
    }

    /**
     * 获取POST方式传递的参数
     *
     * @param fullHttpRequest
     * @return
     */
    public static Map<String, Object> getPostParamsFromChannel(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<String, Object>(16);
        if (fullHttpRequest.method() == HttpMethod.POST) {
            // 处理POST请求
            String strContentType = fullHttpRequest.headers().get(CONTENT_TYPE).toString().trim();
            if (strContentType.contains("x-www-form-urlencoded")) {
                params = getFormParams(fullHttpRequest);
            } else if (strContentType.contains("application/json")) {
                try {
                    params = getJsonParams(fullHttpRequest);
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            } else {
                return null;
            }
            return params;
        } else {
            return null;
        }
    }

    /**
     * 解析from表单数据（Content-Type = x-www-form-urlencoded）
     *
     * @param fullHttpRequest
     * @return
     */
    public static Map<String, Object> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<String, Object>(16);

        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();

        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }
        return params;
    }

    /**
     * 解析json数据（Content-Type =application/json）
     *
     * @param fullHttpRequest
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Map<String, Object> getJsonParams(FullHttpRequest fullHttpRequest) throws UnsupportedEncodingException {
        Map<String, Object> params = new HashMap<String, Object>(16);

        ByteBuf content = fullHttpRequest.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        String strContent = new String(reqContent, CharsetUtil.UTF_8);

        JSONObject jsonParams = JSON.parseObject(strContent);
        for (Object key : jsonParams.keySet()) {
            params.put(key.toString(), jsonParams.get(key));
        }
        return params;
    }

    /**
     * 向客户端发送错误信息
     *
     * @param ctx
     * @param status
     */
    public static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept");
        response.headers().set(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST");
        response.headers().set(CONTENT_TYPE, "application/json;charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes() + "");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    /**
     * 向客户端发送错误信息
     */
    public static void sendResp(ChannelHandlerContext ctx, Object obj, FullHttpRequest req) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept");
        response.headers().set(CONTENT_TYPE, "application/json;charset=UTF-8");
        ByteBuf buffer;
        if (obj instanceof String) {
            buffer = Unpooled.copiedBuffer((String) obj, CharsetUtil.UTF_8);
        } else {
            String content = JSON.toJSONString(obj);
            buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        }
        response.content().writeBytes(buffer);
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes() + "");
        buffer.release();
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture future = ctx.writeAndFlush(response);

        future.addListener(ChannelFutureListener.CLOSE);
    }

}
