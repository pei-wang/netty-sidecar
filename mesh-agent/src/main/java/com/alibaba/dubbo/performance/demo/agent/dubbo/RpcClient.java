package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client.SimpleCallback;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);
    private ConnecManager connectManager;
    private ConcurrentMap<Long, SimpleCallback<RpcResponse>> callbackMap = new ConcurrentHashMap<>();
    private static IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private static RpcClient rpcClient;
    private static long NstartTime;

    public static RpcClient getInstance() {
        if (rpcClient == null) {
            synchronized (RpcClient.class) {
                rpcClient = new RpcClient(registry);
            }
        }
        return rpcClient;
    }

    private RpcClient(IRegistry registry) {
        this.connectManager = new ConnecManager();
    }

    public void handleResponse(RpcResponse rpcResponse) {
        if ((System.currentTimeMillis() - NstartTime) > 1000) {
            logger.info("callback size:{}", callbackMap.size());
        }
        SimpleCallback<RpcResponse> simpleCallback = callbackMap.remove(Long.parseLong(rpcResponse.getRequestId()));
        if (simpleCallback != null) {
            simpleCallback.operationComplete(rpcResponse, null);
        }
    }

    public void invoke(String interfaceName, String method, String parameterTypesString, String parameter, SimpleCallback<RpcResponse> simpleCallback) throws Exception {

        Channel channel = connectManager.getChannel();

        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(method);
        invocation.setAttachment("path", interfaceName);
        invocation.setParameterTypes(parameterTypesString);    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(parameter, writer);
        invocation.setArguments(out.toByteArray());

        Request request = new Request();
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);

        logger.info("requestId=" + request.getId());
        callbackMap.put(request.getId(), simpleCallback);
        channel.writeAndFlush(request);
    }
}
