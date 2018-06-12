package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.AgentClientFuture;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentDecoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentEncoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);
    private Bootstrap bootstrap;
    private Channel[] channels;
    private Random random = new Random();
    private static NettyClient instance = null;
    private final ConcurrentMap<Long, SimpleCallback<AgentResponse>> callbackMap = new ConcurrentHashMap<>();

    public static NettyClient getInstance() {
        if (instance == null) {
            synchronized (NettyClient.class) {
                if (instance == null) {
                    instance = new NettyClient();
                }
            }
        }
        return instance;
    }

    public NettyClient() {
        try {
            build();
        } catch (Exception e) {
            LOGGER.error("launch..");
        }
    }

    private void build() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                .addLast("encoder", new LengthFieldPrepender(4, false))
                                .addLast(new AgentDecoder(AgentResponse.class))
                                .addLast(new AgentEncoder(AgentRequest.class))
                                .addLast(new ClientHandler());
                    }
                });
        IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
        List<Endpoint> endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
        int sizes = 0;
        for (Endpoint endpoint : endpoints) {
            if (endpoint.getWeight() > 1) {
                sizes = sizes + endpoint.getWeight() + 1;
            } else {
                sizes = sizes + endpoint.getWeight();
            }
        }
        channels = new Channel[sizes];
        int index = 0;
        for (Endpoint endpoint : endpoints) {
            LOGGER.info("trying to connect endpoint{}:{}", endpoint.getHost(), endpoint.getPort());
            int weight = endpoint.getWeight();
            if (weight > 1) {
                weight = weight + 1;
            }
            for (int i = 0; i < weight; i++) {
                LOGGER.info("connected to endpoint:{}:{}", endpoint.getHost(), endpoint.getPort());
                channels[index] = bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel();
                index++;
            }
        }
    }

    public void sendData(AgentRequest agentRequest, SimpleCallback<AgentResponse> callback) {
        long startTime = System.currentTimeMillis();
        callbackMap.put(agentRequest.getTraceId(), callback);
        LOGGER.info("Request-traceId:{} The time access sendData:{}", agentRequest.getTraceId(), System.currentTimeMillis());
        AgentClientFuture agentClientFuture = new AgentClientFuture();
        AgentClientRequestHolder.put(String.valueOf(agentRequest.getTraceId()), agentClientFuture);
        int o1 = random.nextInt(channels.length);
        LOGGER.info("channel size:{}, random number:{}", channels.length, o1);

        Channel channel = channels[o1];
        channel.writeAndFlush(agentRequest);
        LOGGER.info("Request-traceId:{} The time get result: {} ms", agentRequest.getTraceId(), System.currentTimeMillis() - startTime);
    }

    public void handleResponse(AgentResponse agentResponse) {
        LOGGER.info("agentResponse in handle {}", agentResponse.getTraceId());
        SimpleCallback<AgentResponse> agentResponseSimpleCallback = callbackMap.remove(Long.parseLong(agentResponse.getTraceId()));
        LOGGER.info("callback:" + agentResponseSimpleCallback);
        if (agentResponseSimpleCallback != null) {
            agentResponseSimpleCallback.operationComplete(agentResponse, null);
        }
    }
}