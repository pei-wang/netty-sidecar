package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.AgentClientFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class AgentClientRequestHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentClientRequestHolder.class);
    private static ConcurrentHashMap<String, AgentClientFuture> processiongRpc = new ConcurrentHashMap<>();
    private static long startTime = System.currentTimeMillis();

    public static void put(String traceId, AgentClientFuture agentClientFuture) {
        processiongRpc.put(traceId, agentClientFuture);
        if ((System.currentTimeMillis() - startTime) > 1000) {
            LOGGER.info("RequestHolder size:{}", processiongRpc.size());
        }
    }

    public static AgentClientFuture get(String traceId) {
        return processiongRpc.get(traceId);
    }

    public static void remove(String traceId) {
        processiongRpc.remove(traceId);
    }
}
