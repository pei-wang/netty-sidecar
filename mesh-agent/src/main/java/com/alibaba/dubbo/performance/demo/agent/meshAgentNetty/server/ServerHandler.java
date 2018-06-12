package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client.SimpleCallback;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
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
//        Worker.dispatch(new DBHandler(agentRequest, channelHandlerContext));
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
                            LOGGER.info("Request-traceId:{} The time get result from dubbo: {} ms", agentRequest.getTraceId(), System.currentTimeMillis() - startTime);
                            agentResponse.setResult(msg.getBytes());
                            channelHandlerContext.writeAndFlush(agentResponse);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}

