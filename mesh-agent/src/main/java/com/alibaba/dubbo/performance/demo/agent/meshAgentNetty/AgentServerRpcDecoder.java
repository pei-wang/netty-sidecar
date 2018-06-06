package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.DubboRpcDecoder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Arrays;
import java.util.List;

public class AgentServerRpcDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        int savedReaderIndex = byteBuf.readerIndex();
        Object msg = null;
        try {
            do {
                msg = decode2(byteBuf);
                if (msg == DubboRpcDecoder.DecodeResult.NEED_MORE_INPUT) {
                    byteBuf.readerIndex(savedReaderIndex);
                    break;
                }
                list.add(msg);
            } while (byteBuf.isReadable());
        } finally {
            if (byteBuf.isReadable()) {
                byteBuf.discardReadBytes();
            }
        }
    }

    private Object decode2(ByteBuf byteBuf) {
        int savedReaderIndex = byteBuf.readerIndex();
        int readable = byteBuf.readableBytes();
        if (readable < 12) {
            return DubboRpcDecoder.DecodeResult.NEED_MORE_INPUT;
        }
        byte[] header = new byte[12];
        byteBuf.readBytes(header);
        int dataLen = Bytes.bytes2int(Arrays.copyOfRange(header, 8, 12));
        int tt = dataLen + 12;
        if (readable < tt) {
            return DubboRpcDecoder.DecodeResult.NEED_MORE_INPUT;
        }

        byteBuf.readerIndex(savedReaderIndex);
        byte[] data = new byte[tt];
        byteBuf.readBytes(data);

        AgentRequest agentRequest = new AgentRequest();
        agentRequest.setId((Bytes.bytes2long(Arrays.copyOfRange(data, 0, 8), 0)));
        agentRequest.setmData(Arrays.copyOfRange(data, 12, tt));
        return agentRequest;
    }
}
