package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.*;
import io.netty.channel.Channel;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private ConnecManager connectManager;

    public RpcClient() {
        this.connectManager = new ConnecManager();
    }

    public Object invoke(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {

        SimpleChannelPool pool = connectManager.getChannel();

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
        long startTime = System.currentTimeMillis();
        RpcFuture future = new RpcFuture();
        RpcRequestHolder.put(String.valueOf(request.getId()), future);
        Future<Channel> f = pool.acquire();
        f.addListener((FutureListener<Channel>) f1 -> {
            if (f1.isSuccess()) {
                Channel ch = f1.getNow();
                logger.info("Request-traceId:{} The time get channel{}: {} ms", request.getId(), ch.id(), System.currentTimeMillis() - startTime);
                ch.writeAndFlush(request);
                pool.release(ch);
            }
        });

        Object result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
