package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends SimpleChannelInboundHandler<AgentRequest> {
    private RpcClient rpcClient = new RpcClient();
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AgentRequest agentRequest) throws Exception {
        LOGGER.info("Request-traceId:{} The time in serverHandler in:{}", agentRequest.getTraceId(), System.currentTimeMillis());
        long startTime = System.currentTimeMillis();
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
        LOGGER.info("Request-traceId:{} The time get result form dubbo: {} ms", agentRequest.getTraceId(), System.currentTimeMillis() - startTime);
        channelHandlerContext.writeAndFlush(agentResponse);
        LOGGER.info("Request-traceId:{} The time get result form dubbo and Sent out: {} ms", agentRequest.getTraceId(), System.currentTimeMillis() - startTime);
        LOGGER.info("Request-traceId:{} The time in serverHandler out:{}", agentRequest.getTraceId(), System.currentTimeMillis());
    }
}
