package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.AgentClientFuture;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<AgentResponse> {
    private final Map<String, BlockingQueue<AgentResponse>> responsesMap = new ConcurrentHashMap<>();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AgentResponse agentResponse) throws Exception {
        AgentClientFuture future = AgentClientRequestHolder.get(agentResponse.getTraceId());
        if (future != null) {
            AgentClientRequestHolder.remove(agentResponse.getTraceId());
            future.done(agentResponse);
        }
    }
}
