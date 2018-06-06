package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import java.util.concurrent.ConcurrentHashMap;

public class ClientHolder {
    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String, ClientFuture> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId, ClientFuture clientFuture) {
        processingRpc.put(requestId, clientFuture);
    }

    public static ClientFuture get(String requestId) {
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId) {
        processingRpc.remove(requestId);
    }
}
