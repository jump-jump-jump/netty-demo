package com.example.demo.service;

import com.example.demo.aspect.LogExceptionPointCut;
import com.example.demo.model.response.common.ResponseResult;
import io.netty.channel.ChannelHandlerContext;

public interface IDeviceService {

    @LogExceptionPointCut
    ResponseResult deviceConnect(String deviceGuid);

    void disConnect(ChannelHandlerContext ctx);

}
