package com.example.demo.util;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * @ClassName UdpUtil
 * @Author zs427
 * @Date 2019-7-30 14:53
 * @Version 1.0
 */
public class UdpUtil {

    public static void sendResp(ChannelHandlerContext ctx, DatagramPacket dp, Object obj) {
        // 由于数据报的数据是以字符数组传的形式存储的，所以传转数据
        ByteBuf buffer;
        if (obj instanceof String) {
            buffer = Unpooled.copiedBuffer((String) obj, CharsetUtil.UTF_8);
        } else {
            String content = JSON.toJSONString(obj);
            buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        }
        DatagramPacket data = new DatagramPacket(buffer, dp.sender());
        //向客户端发送消息
        ctx.writeAndFlush(data);
    }

}
