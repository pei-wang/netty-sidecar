package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandler extends SimpleChannelInboundHandler<AgentRequest> {
    private RpcClient rpcClient = new RpcClient();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AgentRequest agentRequest) throws Exception {
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setTraceId(String.valueOf(agentRequest.getTraceId()));

        try{
            Object result = rpcClient.invoke(agentRequest.getAgentRpcInvocation().getInterfaceName(),
                    agentRequest.getAgentRpcInvocation().getMethod(),
                    agentRequest.getAgentRpcInvocation().getPrameterTypesString(),
                    agentRequest.getAgentRpcInvocation().getPrameter());
            agentResponse.setResult(result);
        }catch (Throwable t){
            agentResponse.setError(t);
        }
        channelHandlerContext.writeAndFlush(agentResponse);

    }
}
