package com.example.demo.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName GlobalWebsocketChannelUtil
 * @Author zs427
 * @Date 2019-7-16 9:39
 * @Version 1.0
 */
@Slf4j
public class GlobalWebsocketChannelUtil {

    /**
     * 保存全局的  连接上服务器的客户
     */
    public static ConcurrentMap<ChannelId, String> channelIdDeviceGuidMap = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, Channel> deviceGuidChannelMap = new ConcurrentHashMap<>();

    public static void closeWsConnectByCtx(ChannelHandlerContext ctx) {
        log.info("【设备断开WS DeviceGuid】====>" + channelIdDeviceGuidMap.get(ctx.channel().id()));
        channelIdDeviceGuidMap.remove(ctx.channel().id());
        deviceGuidChannelMap.values().remove(ctx.channel());
    }

    public static void closeWsConnectByDeviceGuid(String deviceGuid) {
        log.info("【设备断开WS DeviceGuid】====>" + deviceGuid);
        deviceGuidChannelMap.remove(deviceGuid);
        channelIdDeviceGuidMap.values().remove(deviceGuid);
    }

}
