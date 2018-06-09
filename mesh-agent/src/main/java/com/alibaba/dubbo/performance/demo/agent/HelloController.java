package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client.NettyClient;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);
    private NettyClient nettyClient;

    public HelloController() throws Exception {
        String type = System.getProperty("type");
        if ("consumer".equals(type)) {
            nettyClient = new NettyClient();
        }
    }

    @RequestMapping(value = "")
    public Object invoke(@RequestParam("interface") String interfaceName,
                         @RequestParam("method") String method,
                         @RequestParam("parameterTypesString") String parameterTypesString,
                         @RequestParam("parameter") String parameter) throws Exception {
        String type = System.getProperty("type");
        if ("consumer".equals(type)) {
            return consumer(interfaceName, method, parameterTypesString, parameter);
        } else {
            return "Environment variable type is needed to set to provider or consumer.";
        }
    }


    public Integer consumer(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {
        long startTime = System.currentTimeMillis();
        AgentRequest agentRequest = new AgentRequest();
        logger.info("Request-traceId:{} access consumer....", agentRequest.getTraceId());
        AgentRpcInvocation agentRpcInvocation = new AgentRpcInvocation();
        agentRpcInvocation.setInterfaceName(interfaceName);
        agentRpcInvocation.setMethod(method);
        agentRpcInvocation.setPrameter(parameter);
        agentRpcInvocation.setPrameterTypesString(parameterTypesString);
        agentRequest.setAgentRpcInvocation(agentRpcInvocation);
        AgentResponse agentResponse = nettyClient.sendData(agentRequest);
        logger.info("Request-traceId:{} The time spent on consumer not include on the waiting time: {} ms", agentRequest.getTraceId(), System.currentTimeMillis() - startTime);
        int result = Integer.parseInt(new String((byte[]) agentResponse.getResult()));
        return result;
    }
}
