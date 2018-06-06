package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.concurrent.*;

public class ClientHandler extends SimpleChannelInboundHandler<AgentResponse> {
    private final Map<String, BlockingQueue<AgentResponse>> responsesMap = new ConcurrentHashMap<>();

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, AgentResponse agentResponse) throws Exception {
        BlockingQueue<AgentResponse> queue = responsesMap.get(agentResponse.getTraceId());
        if (queue == null) {
            queue = new LinkedBlockingDeque<>(1);
            responsesMap.putIfAbsent(agentResponse.getTraceId(), queue);
        }
        queue.add(agentResponse);
    }

    public AgentResponse send(AgentRequest request, Pair<Long, TimeUnit> timeout) throws InterruptedException {
        responsesMap.putIfAbsent(request.getTraceId(), new LinkedBlockingQueue<AgentResponse>(1));
        AgentResponse response = null;
        try {
            BlockingQueue<AgentResponse> queue = responsesMap.get(request.getTraceId());
            if (timeout == null) {
                response = queue.take();
            } else {
                response = queue.poll(timeout.getKey(), timeout.getValue());
            }
        } finally {
            responsesMap.remove(request.getTraceId());
        }
        return response;

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }
}
