package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentDecoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentEncoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyChannelPoolHandler implements ChannelPoolHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(NettyChannelPoolHandler.class);
    private ClientHandler clientHandler = new ClientHandler();

    @Override
    public void channelReleased(Channel channel) throws Exception {
        LOGGER.info("channelReleased. Channel ID: " + channel.id());
    }

    @Override
    public void channelAcquired(Channel channel) throws Exception {
        LOGGER.info("channelAcquired. Channel ID: " + channel.id());
    }

    @Override
    public void channelCreated(Channel channel) throws Exception {
        channel.pipeline()
                //处理分包传输问题
                .addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                .addLast("encoder", new LengthFieldPrepender(4, false))
                .addLast(new AgentDecoder(AgentResponse.class))
                .addLast(new AgentEncoder(AgentRequest.class))
                .addLast(clientHandler);
    }
}
