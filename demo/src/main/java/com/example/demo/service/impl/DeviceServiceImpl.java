package com.example.demo.service.impl;

import com.example.demo.model.response.common.CommonCode;
import com.example.demo.model.response.common.CommonResponseResult;
import com.example.demo.model.response.common.ResponseResult;
import com.example.demo.service.IDeviceService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

import static com.example.demo.util.GlobalWebsocketChannelUtil.*;

/**
 * @ClassName DeviceServiceImpl
 * @Author zs427
 * @Date 2019-7-27 10:16
 * @Version 1.0
 */
@Service
public class DeviceServiceImpl implements IDeviceService {

    @Override
    public ResponseResult deviceConnect(String deviceGuid) {
        // TODO 可对携带参数进行校验
        if (deviceGuidChannelMap.containsKey(deviceGuid)) {
            // 存在
            Channel channel = deviceGuidChannelMap.get(deviceGuid);
            if (null != channel && channel.isOpen()) {
                // 已连接，先断开连接
                channel.close();
            } else {
                // 连接已断开，移出 map 中的记录
                closeWsConnectByDeviceGuid(deviceGuid);
            }
        }
        return new CommonResponseResult<>(CommonCode.SUCCESS, deviceGuid);
    }

    @Override
    public void disConnect(ChannelHandlerContext ctx) {
        closeWsConnectByCtx(ctx);
    }

}
