package com.alibaba.dubbo.performance.demo.agent.dubbo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelPoolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcChannelPoolHandler implements ChannelPoolHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(ChannelPoolHandler.class);

    @Override
    public void channelReleased(Channel channel) throws Exception {
        LOGGER.info("channelReleased. Channel ID: " + channel.id());
    }

    @Override
    public void channelAcquired(Channel channel) throws Exception {
        LOGGER.info("channelAcquired. Channel ID: " + channel.id());
    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new DubboRpcEncoder());
        pipeline.addLast(new DubboRpcDecoder());
        pipeline.addLast(new RpcClientHandler());
    }
}
