package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HelloControllerTest {

    @Test
    public void consumer() throws Exception {
        HelloController helloController = new HelloController();
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(new Endpoint("127.0.0.1", 10080));
        helloController.setEndpoints(endpointList);

        int a = helloController.consumer("IHelloService", "hi", "String", "world");
        System.out.println(a);
    }
}
