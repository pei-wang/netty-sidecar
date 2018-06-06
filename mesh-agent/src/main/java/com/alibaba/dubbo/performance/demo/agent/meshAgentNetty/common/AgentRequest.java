package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common;

public class AgentRequest {
    private String traceId;
    private AgentRpcInvocation agentRpcInvocation;

    public AgentRequest() {
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public AgentRpcInvocation getAgentRpcInvocation() {
        return agentRpcInvocation;
    }

    public void setAgentRpcInvocation(AgentRpcInvocation agentRpcInvocation) {
        this.agentRpcInvocation = agentRpcInvocation;
    }
}
