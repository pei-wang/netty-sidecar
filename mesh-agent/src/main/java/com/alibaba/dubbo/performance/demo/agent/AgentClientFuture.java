package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;

import java.util.concurrent.*;

public class AgentClientFuture implements Future<AgentResponse> {
    private CountDownLatch latch = new CountDownLatch(1);
    private AgentResponse agentResponse;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public AgentResponse get() throws InterruptedException, ExecutionException {
        latch.await();
        return this.agentResponse;
    }

    @Override
    public AgentResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        return agentResponse;
    }

    public void done(AgentResponse agentResponse) {
        this.agentResponse = agentResponse;
        latch.countDown();
    }
}
