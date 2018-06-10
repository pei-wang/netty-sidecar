package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.channel.ChannelHandlerContext;

public class DBHandler implements Runnable {
    private static final RpcClient rpcClient = new RpcClient();
    private AgentRequest agentRequest;
    private ChannelHandlerContext chx;

    public DBHandler(AgentRequest agentRequest, ChannelHandlerContext chx) {
        this.agentRequest = agentRequest;
        this.chx = chx;
    }

    public AgentRequest getAgentRequest() {
        return agentRequest;
    }

    public void setAgentRequest(AgentRequest agentRequest) {
        this.agentRequest = agentRequest;
    }

    public ChannelHandlerContext getChx() {
        return chx;
    }

    public void setChx(ChannelHandlerContext chx) {
        this.chx = chx;
    }

    public void handle(AgentRequest agentRequest) {

    }

    @Override
    public void run() {
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setTraceId(String.valueOf(agentRequest.getTraceId()));
        try {
            Object result = rpcClient.invoke(agentRequest.getAgentRpcInvocation().getInterfaceName(),
                    agentRequest.getAgentRpcInvocation().getMethod(),
                    agentRequest.getAgentRpcInvocation().getPrameterTypesString(),
                    agentRequest.getAgentRpcInvocation().getPrameter());
            agentResponse.setResult(result);
        } catch (Throwable t) {
            agentResponse.setError(t);
        }
    }
}
