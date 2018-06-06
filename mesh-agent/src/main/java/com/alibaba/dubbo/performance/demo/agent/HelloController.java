package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.*;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);
    private Random random = new Random();
    private List<Endpoint> endpoints = null;
    private Object lock = new Object();
    private OkHttpClient httpClient = new OkHttpClient();
//    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    @RequestMapping(value = "")
    public Object invoke(@RequestParam("interface") String interfaceName,
                         @RequestParam("method") String method,
                         @RequestParam("parameterTypesString") String parameterTypesString,
                         @RequestParam("parameter") String parameter) throws Exception {
        return consumer(interfaceName, method, parameterTypesString, parameter);
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public Integer consumer(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {

        if (null == endpoints) {
            synchronized (lock) {
                if (null == endpoints) {
//                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                }
            }
        }

        // 简单的负载均衡，随机取一个
//        Endpoint endpoint = endpoints.get(random.nextInt(endpoints.size()));
        Endpoint endpoint = new Endpoint("127.0.0.1",10080);
        Channel channel = new AgentClientManager().getChannel(endpoint.getHost(), endpoint.getPort());
        StringBuffer sb = new StringBuffer(interfaceName);
        sb.append(";").append(method).append(";").append(parameterTypesString).append(";").append(parameter);
        byte[] req = sb.toString().getBytes();
        ByteBuf messageBuffer = Unpooled.buffer(req.length);
        messageBuffer.writeBytes(req);

        AgentRequest agentRequest = new AgentRequest();
        AgentRpcInvocation agentRpcInvocation = new AgentRpcInvocation();
        agentRpcInvocation.setInterfaceName(interfaceName);
        agentRpcInvocation.setMethod(method);
        agentRpcInvocation.setPrameter(parameter);
        agentRpcInvocation.setPrameterTypesString(parameterTypesString);
        agentRequest.setmData(agentRpcInvocation);

        ClientFuture clientFuture = new ClientFuture();
        ClientHolder.put(String.valueOf(agentRequest.getId()), clientFuture);
        channel.writeAndFlush(agentRequest);
        return Integer.parseInt((String) clientFuture.get());
//        RequestBody requestBody = new FormBody.Builder()
//                .add("interface", interfaceName)
//                .add("method", method)
//                .add("parameterTypesString", parameterTypesString)
//                .add("parameter", parameter)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//
//        try (Response response = httpClient.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            byte[] bytes = response.body().bytes();
//            String s = new String(bytes);
//            return Integer.valueOf(s);
//        }

    }
}
