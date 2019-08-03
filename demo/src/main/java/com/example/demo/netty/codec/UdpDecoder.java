package com.example.demo.netty.codec;

import com.example.demo.model.udp.UdpBaseReceiveDTO;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.AppendableCharSequence;

import java.util.List;

/**
 * @ClassName UdpDecoder
 * @Author zs427
 * @Date 2019-7-31 9:56
 * @Version 1.0
 */
public class UdpDecoder extends MessageToMessageDecoder<DatagramPacket> {

    private enum State {  //2
        Id,
        Token,
        msg
    }

    private State state = State.Id;
    private String id;
    private String token;
    private String msg;

    private final int idSize;
    private final int tokenSize;
    private final UdpDecoder.SeparatorParser separatorParser;

    public UdpDecoder(int idSize, int tokenSize) {
        super();
        this.idSize = idSize;
        this.tokenSize = tokenSize;
        AppendableCharSequence msg = new AppendableCharSequence(128);
        separatorParser = new UdpDecoder.SeparatorParser(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket dp, List out) throws Exception {
        ByteBuf in = dp.content();
        switch (state) {
            case Id:
                if (in.readableBytes() < idSize) {
                    state = State.Id;
                    return;
                }
                in.readBytes(idSize).array();
                id = in.readBytes(idSize).toString(CharsetUtil.UTF_8);
                state = State.Token;
            case Token:
                if (in.readableBytes() < tokenSize) {
                    state = State.Id;
                    return;
                }
                token = in.readBytes(tokenSize).toString(CharsetUtil.UTF_8);
                state = State.msg;
            case msg:
                /*AppendableCharSequence parse = this.separatorParser.parse(in);
                if (null == parse) {
                    state = State.Id;
                    return;
                }
                msg = parse.toString();*/
                msg = in.readBytes(in.readableBytes()).toString(CharsetUtil.UTF_8);
                UdpBaseReceiveDTO dto = new UdpBaseReceiveDTO(id, token, msg);
                ByteBuf content = Unpooled.copiedBuffer(JSON.toJSONString(dto).getBytes());
                out.add(new DatagramPacket(content, dp.recipient(), dp.sender()));
                state = State.Id;
        }
    }

    private static final class SeparatorParser extends UdpDecoder.MsgParser {
        SeparatorParser(AppendableCharSequence msg) {
            super(msg);
        }

        @Override
        public AppendableCharSequence parse(ByteBuf buffer) {
            return super.parse(buffer);
        }
    }

    private static class MsgParser implements ByteBufProcessor {
        private final AppendableCharSequence msg;

        MsgParser(AppendableCharSequence msg) {
            this.msg = msg;
        }

        public AppendableCharSequence getMsg() {
            return msg;
        }

        public AppendableCharSequence parse(ByteBuf buffer) {
            this.msg.reset();
            int i = buffer.forEachByte(this);
            if (i == -1) {
                return null;
            } else {
                buffer.readerIndex(i + 1);
                return this.msg;
            }
        }

        @Override
        public boolean process(byte value) throws Exception {
            char nextByte = (char) value;
            if (nextByte == ']') {
                return false;
            } else {
                this.msg.append(nextByte);
                return true;
            }
        }
    }

}
