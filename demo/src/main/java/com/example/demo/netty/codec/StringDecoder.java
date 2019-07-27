package com.example.demo.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @ClassName StringDecoder
 * @Author zs427
 * @Date 2019-7-27 16:51
 * @Version 1.0
 */
public class StringDecoder extends MessageToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {
        channelHandlerContext.channel().writeAndFlush(o);
    }
}
