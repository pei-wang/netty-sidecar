package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common;

import com.alibaba.dubbo.performance.demo.agent.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentEncoder extends MessageToByteEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEncoder.class);
    private Class<?> genericClass;

    public AgentEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf byteBuf) throws Exception {
        long startTime = System.currentTimeMillis();
        if (genericClass.isInstance(obj)) {
            byte[] data = SerializationUtil.serialize(obj);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
        LOGGER.info("The time on encode: {} ms", System.currentTimeMillis() - startTime);
    }
}
