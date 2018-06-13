package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentDecoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentEncoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NettyClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);
    private Bootstrap bootstrap;
    private Invoker[] weightInvokers;
    private Invoker[] invokers;
    private Random random = new Random();
    private static NettyClient instance = null;
    private final ConcurrentMap<Long, SimpleCallback<AgentResponse>> callbackMap = new ConcurrentHashMap<>();
    private static long NstartTime = System.currentTimeMillis();

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
        EventLoopGroup workerGroup = new EpollEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(EpollSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3 * 1000)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
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
        invokers = new Invoker[3];
        int index = 0;
        int size = 0;
        for (Endpoint endpoint : endpoints) {
            invokers[index] = new Invoker(endpoint, bootstrap.connect(endpoint.getHost(), endpoint.getPort()).sync().channel());
            index++;
            size = size + endpoint.getWeight();
        }
        weightInvokers = new Invoker[size];
        int indexj = 0;
        for (Invoker invoker : invokers) {
            for (int i = 0; i < invoker.getWeight(); i++) {
                weightInvokers[indexj] = invoker;
                indexj++;
            }
        }

        LOGGER.info("channel size:{}", weightInvokers.length);
    }

    public void sendData(AgentRequest agentRequest, SimpleCallback<AgentResponse> callback) {
        callbackMap.put(agentRequest.getTraceId(), callback);
        LOGGER.info("Request-traceId:{} The time access sendData:{}", agentRequest.getTraceId(), System.currentTimeMillis());
        int o1 = random.nextInt(weightInvokers.length);
        Channel channel = weightInvokers[o1].getChannel();
        channel.writeAndFlush(agentRequest);
    }

    public void handleResponse(AgentResponse agentResponse) {
        LOGGER.info("Request-traceId:{} The time access handleResponse:{}", agentResponse.getTraceId(), System.currentTimeMillis());
        SimpleCallback<AgentResponse> agentResponseSimpleCallback = callbackMap.remove(Long.parseLong(agentResponse.getTraceId()));
        LOGGER.info("callback:" + agentResponseSimpleCallback);
        if (agentResponseSimpleCallback != null) {
            agentResponseSimpleCallback.operationComplete(agentResponse, null);
        }
        if ((System.currentTimeMillis() - NstartTime) > 1000) {
            LOGGER.info("callback size:{}", callbackMap.size());
        }

    }
}