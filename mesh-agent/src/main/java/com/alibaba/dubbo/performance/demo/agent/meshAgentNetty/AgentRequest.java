package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import java.util.concurrent.atomic.AtomicLong;

public class
AgentRequest {
    private static AtomicLong atomicLong = new AtomicLong();
    private long id;
    private Object mData;

    public AgentRequest() {
        id = atomicLong.getAndIncrement();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Object getmData() {
        return mData;
    }

    public void setmData(Object mData) {
        this.mData = mData;
    }
}
