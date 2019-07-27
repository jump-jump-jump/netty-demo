package com.example.demo.config;

import com.example.demo.netty.restful.RestProcessor;
import com.example.demo.netty.server.ChannelPipelineInit;
import com.example.demo.netty.server.NettyNioServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @ClassName NettyConfig
 * @Author zs427
 * @Date 2019-7-20 22:53
 * @Version 1.0
 */
@Slf4j
@Configuration
public class NettyConfig implements ApplicationListener<ContextRefreshedEvent> {

    public static int nettyPort;

    @Value("${netty.port}")
    public void setNettyPort(int nettyPort) {
        NettyConfig.nettyPort = nettyPort;
    }

    @Value("${netty.rest.package:com.example.demo.controller}")
    private String scanPackage;

    private void initHttpRestServer() {
        try {
            ChannelPipelineInit pipelineInit =
//                    new ChannelPipelineInit(false, false, SslContextUtil.getSslContext());
                    new ChannelPipelineInit();
            NettyNioServer nettyNio = new NettyNioServer(pipelineInit);
            log.info("netty server start on port " + nettyPort);
            nettyNio.startServer(nettyPort);
        } catch (Exception e) {
            log.info("netty server start failure");
            e.printStackTrace();
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            //root application context 没有parent，他就是老大.
            //需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
            RestProcessor.initScanPackage(scanPackage);
            initHttpRestServer();
        }
    }
}
