package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.AgentClientFuture;

import java.util.concurrent.ConcurrentHashMap;

public class AgentClientRequestHolder {
    private static ConcurrentHashMap<String, AgentClientFuture> processiongRpc = new ConcurrentHashMap<>();

    public static void put(String traceId, AgentClientFuture agentClientFuture) {
        processiongRpc.put(traceId, agentClientFuture);
    }

    public static AgentClientFuture get(String traceId) {
       return processiongRpc.get(traceId);
    }

    public static void remove(String traceId) {
        processiongRpc.remove(traceId);
    }
}
