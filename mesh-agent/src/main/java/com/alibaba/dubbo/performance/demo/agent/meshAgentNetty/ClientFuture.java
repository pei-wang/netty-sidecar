package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import java.util.concurrent.*;

public class ClientFuture implements Future<Object> {
    private CountDownLatch latch = new CountDownLatch(1);
    private String result;

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
    public Object get() throws InterruptedException, ExecutionException {
        latch.await();
        return result;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    public void done(String result) {
        this.result = result;
        latch.countDown();
    }
}
