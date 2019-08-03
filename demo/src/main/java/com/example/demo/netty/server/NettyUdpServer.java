package com.example.demo.netty.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @ClassName NettyUdpServer
 * @Author zs427
 * @Date 2019-7-30 14:12
 * @Version 1.0
 */
public class NettyUdpServer {

    private final ChannelHandler handlers;

    public NettyUdpServer(ChannelHandler channelInitializer) {
        this.handlers = channelInitializer;
    }

    public void startServer(int port) {
        //服务端需要1个线程组
        EventLoopGroup work = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            //服务器 配置
            bootstrap.group(work)
                    .channel(NioDatagramChannel.class)
                    //支持广播
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(handlers);
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭资源
            work.shutdownGracefully();
        }
    }

}
