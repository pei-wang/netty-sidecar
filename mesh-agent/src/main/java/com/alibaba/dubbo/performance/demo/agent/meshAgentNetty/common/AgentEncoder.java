package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common;

import com.alibaba.dubbo.performance.demo.agent.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class AgentEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public AgentEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(obj)) {
            byte[] data = SerializationUtil.serialize(obj);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }
}
