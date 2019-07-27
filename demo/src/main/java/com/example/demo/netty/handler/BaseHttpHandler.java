package com.example.demo.netty.handler;

import com.example.demo.constant.StringConstant;
import com.example.demo.netty.restful.RestProcessor;
import com.example.demo.service.IDeviceService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.SpringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpHeaderNames.UPGRADE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

/**
 * @ClassName WebsocketHandler
 * @Author zs427
 * @Date 2019-7-16 18:06
 * @Version 1.0
 */
@Slf4j
abstract class BaseHttpHandler extends SimpleChannelInboundHandler<Object> {

    protected WebSocketServerHandshaker handshaker;

    protected static IDeviceService deviceService;

    static {
        deviceService = SpringUtil.getBean(IDeviceService.class);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            if (!req.decoderResult().isSuccess()) {
                HttpUtil.sendError(ctx, BAD_REQUEST);
                return;
            }
            if (StringConstant.STRING_WEBSOCKET.equals(req.headers().get(UPGRADE))
                    && req.decoderResult().isSuccess()) {
                websocketHandler(ctx, msg);
            } else {
                RestProcessor.getInstance().invoke(ctx, req);
            }
        }
        if (msg instanceof WebSocketFrame) {
            websocketHandler(ctx, msg);
        }
    }

    /**
     * 断开连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        deviceService.disConnect(ctx);
    }

    /**
     * 连接异常   需要关闭相关资源
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        ctx.channel().close();
    }

    /**
     * 这里只要完成 flush
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public abstract void websocketHandler(ChannelHandlerContext ctx, Object msg);

}
