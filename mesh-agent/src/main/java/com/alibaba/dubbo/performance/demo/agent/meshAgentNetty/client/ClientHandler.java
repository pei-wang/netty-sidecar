package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.AgentClientFuture;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<AgentResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AgentResponse agentResponse) throws Exception {
        AgentClientFuture future = AgentClientRequestHolder.get(agentResponse.getTraceId());
        LOGGER.info("Request-traceId:{} The time in clientHandler:{}", agentResponse.getTraceId(), System.currentTimeMillis());
        if (future != null) {
            AgentClientRequestHolder.remove(agentResponse.getTraceId());
            future.done(agentResponse);
        }
    }
}
