package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;
import sun.nio.ch.Net;

public class ClientTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientTask.class);

    private String interfaceName;
    private String method;
    private String parameterTypesString;
    private String parameter;
    private DeferredResult<Integer> deferredResult;

    public ClientTask(String interfaceName, String method,
                      String parameterTypesString, String parameter,
                      DeferredResult<Integer> deferredResult) {
        this.interfaceName = interfaceName;
        this.method = method;
        this.parameterTypesString = parameterTypesString;
        this.parameter = parameter;
        this.deferredResult = deferredResult;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        AgentRequest agentRequest = new AgentRequest();
        LOGGER.info("Request-traceId:{} access consumer....", agentRequest.getTraceId());
        AgentRpcInvocation agentRpcInvocation = new AgentRpcInvocation();
        agentRpcInvocation.setInterfaceName(interfaceName);
        agentRpcInvocation.setMethod(method);
        agentRpcInvocation.setPrameter(parameter);
        agentRpcInvocation.setPrameterTypesString(parameterTypesString);
        agentRequest.setAgentRpcInvocation(agentRpcInvocation);
        NettyClient.getInstance().sendData(agentRequest, new SimpleCallback<AgentResponse>() {
            @Override
            public void operationComplete(AgentResponse msg, Throwable t) {
                LOGGER.info("Request-traceId:{} The time access callback:{}", msg.getTraceId(), System.currentTimeMillis());
                int result = Integer.parseInt(new String((byte[]) msg.getResult()));
                LOGGER.info("The Request-traceId:{} takes {} ms to finished", msg.getTraceId(), System.currentTimeMillis() - startTime);
                deferredResult.setResult(result);
            }
        });
    }
}
