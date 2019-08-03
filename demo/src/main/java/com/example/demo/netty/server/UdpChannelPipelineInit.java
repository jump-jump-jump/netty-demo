package com.example.demo.netty.server;

import com.example.demo.netty.codec.UdpDecoder;
import com.example.demo.netty.handler.UdpHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

/**
 * @ClassName UdpChannelPipelinelnit
 * @Author zs427
 * @Date 2019-7-30 14:17
 * @Version 1.0
 */
public class UdpChannelPipelineInit extends ChannelInitializer<Channel> {

    private final int idSize;
    private final int tokenSize;

    public UdpChannelPipelineInit(int idSize, int tokenSize) {
        this.idSize = idSize;
        this.tokenSize = tokenSize;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("udp-decoder", new UdpDecoder(idSize, tokenSize));
        pipeline.addLast("udp-handler", new UdpHandler());
    }

}
