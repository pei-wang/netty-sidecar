package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends SimpleChannelInboundHandler<AgentResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AgentResponse agentResponse) throws Exception {
        LOGGER.info("Request-traceId:{} The time access clientHandler:{}", agentResponse.getTraceId(), System.currentTimeMillis());
        NettyClient.getInstance().handleResponse(agentResponse);
    }
}
