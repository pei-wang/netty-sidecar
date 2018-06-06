package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client.NettyClientFactory;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRpcInvocation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);

    public HelloController() throws Exception {
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

        AgentRequest agentRequest = new AgentRequest();
        AgentRpcInvocation agentRpcInvocation = new AgentRpcInvocation();
        agentRpcInvocation.setInterfaceName(interfaceName);
        agentRpcInvocation.setMethod(method);
        agentRpcInvocation.setPrameter(parameter);
        agentRpcInvocation.setPrameterTypesString(parameterTypesString);
        agentRequest.setTraceId(UUID.randomUUID().toString());
        agentRequest.setAgentRpcInvocation(agentRpcInvocation);
        return Integer.parseInt(new String((byte[]) NettyClientFactory.get().asyncSend(agentRequest, Pair.of(2000L, TimeUnit.MILLISECONDS)).getResult()));

    }
}
