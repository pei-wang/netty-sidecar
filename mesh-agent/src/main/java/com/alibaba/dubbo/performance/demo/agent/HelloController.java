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
    public static ExecutorService executorService = new ThreadPoolExecutor(120, 120, 1000L, TimeUnit.MILLISECONDS, tasks);
    private static long startTime = System.currentTimeMillis();

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
            new ClientTask(interfaceName, method, parameterTypesString, parameter, deferredResult).run();
            if ((System.currentTimeMillis() - startTime) > 1000) {
                long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;//已使用内存
                long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;//总共可使用内存
                System.out.printf("可用内存:%sm ", freeMemory);
                System.out.printf("可用总内存:%sm \n", totalMemory);
            }

            return deferredResult;
        }
        return null;
    }
}
