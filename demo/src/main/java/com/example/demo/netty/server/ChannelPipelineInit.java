package com.example.demo.netty.server;

import com.example.demo.netty.handler.WebsocketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;


/**
 * @author bruce - 2018/2/5 13:32
 */
public class ChannelPipelineInit extends ChannelInitializer<Channel> {

    private SSLContext sslContext;
    private boolean clientMode;
    private boolean needClientAuth;

    public ChannelPipelineInit() {
        this(false, false, null);
    }

    public ChannelPipelineInit(String scanPackage, SSLContext sslContext, boolean clientMode) {
        this(clientMode, false, sslContext);
    }

    public ChannelPipelineInit(boolean needClientAuth, SSLContext sslContext) {
        this(false, needClientAuth, sslContext);
    }

    public ChannelPipelineInit(boolean clientMode,
                               boolean needClientAuth, SSLContext sslContext) {
        this.sslContext = sslContext;
        this.clientMode = clientMode;
        this.needClientAuth = needClientAuth;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(clientMode);
            sslEngine.setNeedClientAuth(needClientAuth);
            pipeline.addFirst("ssl", new SslHandler(sslEngine));
        }
        // HTTP请求编解码器
        pipeline.addLast("http-codec", new HttpServerCodec());
        // 将HTTP消息的多个部分合成一条完整的HTTP消息
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
        // 解决大码流的问题，ChunkedWriteHandler：向客户端发送HTML5文件
        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
        // 配置通道处理  来进行业务处理
        pipeline.addLast("http-handler", new WebsocketHandler());
    }
}
