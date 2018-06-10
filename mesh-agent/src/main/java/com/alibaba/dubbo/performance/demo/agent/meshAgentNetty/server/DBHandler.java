package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBHandler implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(DBHandler.class);
    private static IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private static final RpcClient rpcClient = new RpcClient(registry);
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
        long startTime = System.currentTimeMillis();
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
        LOGGER.info("Request-traceId:{} The time get result form dubbo: {} ms", agentRequest.getTraceId(), System.currentTimeMillis() - startTime);
        chx.writeAndFlush(agentResponse);
    }
}
