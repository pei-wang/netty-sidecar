package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common;

import java.util.concurrent.atomic.AtomicLong;

public class AgentRequest {
    private static AtomicLong atomicLong = new AtomicLong();
    private long id;
    private AgentRpcInvocation agentRpcInvocation;

    public AgentRequest() {
        id = atomicLong.getAndIncrement();
    }

    public long getTraceId() {
        return id;
    }

    public AgentRpcInvocation getAgentRpcInvocation() {
        return agentRpcInvocation;
    }

    public void setAgentRpcInvocation(AgentRpcInvocation agentRpcInvocation) {
        this.agentRpcInvocation = agentRpcInvocation;
    }
}
