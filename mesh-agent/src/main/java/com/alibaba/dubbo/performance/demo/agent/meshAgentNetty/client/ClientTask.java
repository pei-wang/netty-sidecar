package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

public class ClientTask implements Runnable {
    private static NettyClient nettyClient = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientTask.class);

    static {
        try {
            nettyClient = new NettyClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        AgentRequest agentRequest = new AgentRequest();
        LOGGER.info("Request-traceId:{} access consumer....", agentRequest.getTraceId());
        AgentRpcInvocation agentRpcInvocation = new AgentRpcInvocation();
        agentRpcInvocation.setInterfaceName(interfaceName);
        agentRpcInvocation.setMethod(method);
        agentRpcInvocation.setPrameter(parameter);
        agentRpcInvocation.setPrameterTypesString(parameterTypesString);
        agentRequest.setAgentRpcInvocation(agentRpcInvocation);
        AgentResponse agentResponse = nettyClient.sendData(agentRequest);
        int result = Integer.parseInt(new String((byte[]) agentResponse.getResult()));
        deferredResult.setResult(result);
    }
}
