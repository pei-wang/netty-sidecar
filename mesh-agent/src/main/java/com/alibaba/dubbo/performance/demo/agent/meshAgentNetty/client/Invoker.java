package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.channel.Channel;

public class Invoker {

    private final Endpoint endpoint;
    private final Channel channel;
    private final int weight;

    public Invoker(Endpoint endpoint, Channel channel) {
        this.endpoint = endpoint;
        this.channel = channel;
        this.weight = endpoint.getWeight();
    }
    
    public void invoke(AgentRequest request) {
        channel.writeAndFlush(request, channel.voidPromise());
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Channel getChannel() {
        return channel;
    }

    public int getWeight() {
        return weight;
    }

}
