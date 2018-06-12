package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client.SimpleCallback;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBHandler implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(DBHandler.class);
    private AgentRequest agentRequest;
    private ChannelHandlerContext chx;

    public DBHandler(AgentRequest agentRequest, ChannelHandlerContext chx) {
        this.agentRequest = agentRequest;
        this.chx = chx;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setTraceId(String.valueOf(agentRequest.getTraceId()));
        try {
            RpcClient.getInstance().invoke(agentRequest.getAgentRpcInvocation().getInterfaceName(),
                    agentRequest.getAgentRpcInvocation().getMethod(),
                    agentRequest.getAgentRpcInvocation().getPrameterTypesString(),
                    agentRequest.getAgentRpcInvocation().getPrameter(), new SimpleCallback<RpcResponse>() {
                        @Override
                        public void operationComplete(RpcResponse msg, Throwable t) {
                            chx.writeAndFlush(msg);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("Request-traceId:{} The time get result form dubbo: {} ms", agentRequest.getTraceId(), System.currentTimeMillis() - startTime);
    }
}
