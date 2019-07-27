package com.example.demo.netty.handler;

import com.example.demo.constant.StringConstant;
import com.example.demo.constant.WebsocketConnectConstant;
import com.example.demo.model.response.common.CommonCode;
import com.example.demo.model.response.common.CommonResponseResult;
import com.example.demo.model.response.common.Response;
import com.example.demo.model.response.common.ResponseResult;
import com.example.demo.util.HttpUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.example.demo.util.GlobalWebsocketChannelUtil.channelIdDeviceGuidMap;
import static com.example.demo.util.GlobalWebsocketChannelUtil.deviceGuidChannelMap;
import static com.example.demo.util.HttpUtil.getAccessUriFormChannel;
import static com.example.demo.util.HttpUtil.getPathParamsFromChannel;
import static io.netty.handler.codec.http.HttpHeaderNames.UPGRADE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

/**
 * @ClassName WebsocketHandler
 * @Author zs427
 * @Date 2019-7-16 18:39
 * @Version 1.0
 */
@Slf4j
public class WebsocketHandler extends BaseHttpHandler {

    @Override
    public void websocketHandler(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            if (StringConstant.STRING_WEBSOCKET.contentEquals(req.headers().get(UPGRADE))
                    && req.decoderResult().isSuccess()) {
                // 升级协议, 建立 ws 连接
                initConnect(ctx, req);
            }
        }
        if (msg instanceof WebSocketFrame) {
            doHandlerWebsocketRequest(ctx, (WebSocketFrame) msg);
        }
    }

    private void initConnect(ChannelHandlerContext ctx, FullHttpRequest msg) {
        // 连接校验
        Response responseResult = connectionCheck(msg);
        if (!(responseResult instanceof CommonResponseResult)) {
            HttpUtil.sendError(ctx, BAD_REQUEST);
            return;
        }
        String deviceGuid = ((String) ((CommonResponseResult) responseResult).getData());

        // 校验通过可以开始建立 websocket 连接
        ctx.attr(AttributeKey.valueOf(StringConstant.STRING_TYPE)).set(msg.uri());
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                WebsocketConnectConstant.WEBSOCKET_CONNECT_PROTOCOL
                        + msg.headers().get(StringConstant.STRING_HOST)
                        + WebsocketConnectConstant.WEBSOCKET_CONNECT_URI, null, false
        );
        handshaker = wsFactory.newHandshaker(msg);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            //进行连接
            handshaker.handshake(ctx.channel(), msg);
            deviceGuidChannelMap.put(deviceGuid, ctx.channel());
            channelIdDeviceGuidMap.put(ctx.channel().id(), deviceGuid);
            log.info("【新设备加入WS DeviceGuid】====>" + deviceGuid);
        }
    }

    /**
     * device 连接参数校验
     *
     * @param msg
     * @return
     */
    private Response connectionCheck(FullHttpRequest msg) {
        String accessUri = getAccessUriFormChannel(msg);
        if (!accessUri.equals(WebsocketConnectConstant.WEBSOCKET_CONNECT_URI)) {
            return new ResponseResult(CommonCode.FAIL);
        }
        Map<String, Object> paramMap = getPathParamsFromChannel(msg);
        String deviceGuid = (String) paramMap.get("DeviceGuid");
        return deviceService.deviceConnect(deviceGuid);
    }

    private void doHandlerWebsocketRequest(ChannelHandlerContext ctx, WebSocketFrame msg) {
        //判断msg 是哪一种类型  分别做出不同的反应
        if (msg instanceof CloseWebSocketFrame) {
            // 解决引用计数报错
            ReferenceCountUtil.retain(msg);
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg);
            return;
        }
        if (!(msg instanceof TextWebSocketFrame)) {
            log.info("【不支持二进制】");
            throw new UnsupportedOperationException("不支持二进制");
        }
        String receiveMsg = ((TextWebSocketFrame) msg).text();
        // TODO 收到客户端 ws 消息 可做业务处理
        ctx.channel().writeAndFlush(new TextWebSocketFrame("已收到客户端消息：" + receiveMsg));
    }

}
