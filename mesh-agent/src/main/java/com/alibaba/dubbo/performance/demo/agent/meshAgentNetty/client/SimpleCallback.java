package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

public interface SimpleCallback<T> {
    void operationComplete(T msg, Throwable t);
}
