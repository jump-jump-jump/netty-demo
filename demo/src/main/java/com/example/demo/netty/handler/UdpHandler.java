package com.example.demo.netty.handler;

import com.alibaba.fastjson.JSON;
import com.example.demo.model.udp.UdpBaseReceiveDTO;
import com.example.demo.util.UdpUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName UdpHandler
 * @Author zs427
 * @Date 2019-7-30 14:15
 * @Version 1.0
 */
@Slf4j
public class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket dp) throws Exception {
        String dtoString = dp.content().toString(CharsetUtil.UTF_8);
        UdpBaseReceiveDTO receiveDTO = JSON.parseObject(dtoString, UdpBaseReceiveDTO.class);
        log.info(receiveDTO.toString());
        UdpUtil.sendResp(ctx, dp, dtoString);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
