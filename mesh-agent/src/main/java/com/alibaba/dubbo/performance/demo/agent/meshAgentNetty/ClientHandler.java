package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends SimpleChannelInboundHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class.getName());

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        AgentResponse agentResponse= (AgentResponse) msg;

        String body = new String(agentResponse.getBytes(), "UTF-8");
        System.out.println("receive data from server:" + body);
        ClientHolder.get(agentResponse.getRequestId()).done(body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.warn("unexpected exception form downstream :" + cause.getMessage());
        ctx.close();
    }
}
