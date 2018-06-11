package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends SimpleChannelInboundHandler<AgentRequest> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    public ServerHandler() throws Exception {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AgentRequest agentRequest) throws Exception {
        LOGGER.info("Request-traceId:{},The arrived time at provider:{}", agentRequest.getTraceId(), System.currentTimeMillis());
        Worker.dispatch(new DBHandler(agentRequest, channelHandlerContext));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}

