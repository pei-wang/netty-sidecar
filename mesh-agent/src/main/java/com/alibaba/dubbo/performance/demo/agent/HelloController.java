package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client.ClientTask;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client.NettyClient;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRpcInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.*;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);
    private static final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    public static ExecutorService executorService = new ThreadPoolExecutor(50, 80, 1000L, TimeUnit.MILLISECONDS, tasks);

    public HelloController() throws Exception {

    }

    @RequestMapping(value = "")
    public DeferredResult<Integer> invoke(@RequestParam("interface") String interfaceName,
                                          @RequestParam("method") String method,
                                          @RequestParam("parameterTypesString") String parameterTypesString,
                                          @RequestParam("parameter") String parameter) throws Exception {
        String type = System.getProperty("type");
        if ("consumer".equals(type)) {
            DeferredResult<Integer> deferredResult = new DeferredResult<>();
            executorService.execute(new ClientTask(interfaceName, method, parameterTypesString, parameter, deferredResult));
            return deferredResult;
        }
        return null;
    }
}
