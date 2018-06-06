package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class AgentServerRpcEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        AgentResponse agentResponse = (AgentResponse) o;
        byte[] header = new byte[12];
        Bytes.long2bytes(Long.valueOf(agentResponse.getRequestId()), header, 0);


        int len = agentResponse.getBytes().length;
        byteBuf.writerIndex(12);
        byteBuf.writeBytes(agentResponse.getBytes());
        Bytes.int2bytes(len, header, 8);


        byteBuf.writerIndex(0);
        byteBuf.writeBytes(header);
        byteBuf.writerIndex(12 + len);
    }
}
