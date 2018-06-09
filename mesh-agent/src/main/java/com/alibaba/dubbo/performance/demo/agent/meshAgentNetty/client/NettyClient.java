package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.AgentClientFuture;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);
    private List<Endpoint> endpoints;

    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    int workerGroupThreads = 10;
    private AtomicInteger pos = new AtomicInteger();
    ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap;
    List<SimpleChannelPool> channelPools = new ArrayList<>();
    public NettyClient() throws Exception {
        build();
        IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
        endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
        for (Endpoint endpoint : endpoints) {
            LOGGER.info("trying to connect endpoint{}:{}", endpoint.getHost(), endpoint.getPort());
            channelPools.add(poolMap.get(new InetSocketAddress(endpoint.getHost(), endpoint.getPort())));
            LOGGER.info("connected to endpoint:{}:{}", endpoint.getHost(), endpoint.getPort());
        }
    }

    public void build() {
        workerGroup = new NioEventLoopGroup(workerGroupThreads);
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);
        poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(InetSocketAddress key) {
                return new FixedChannelPool(bootstrap.remoteAddress(key), new NettyChannelPoolHandler(), 8);
            }
        };
    }

    public AgentResponse sendData(AgentRequest agentRequest) {
        AgentClientFuture agentClientFuture = new AgentClientFuture();
        AgentClientRequestHolder.put(String.valueOf(agentRequest.getTraceId()), agentClientFuture);
        SimpleChannelPool pool = channelPools.get(pos.getAndIncrement() % channelPools.size());
        LOGGER.info("poolUsed:" + pool);
        Future<Channel> f = pool.acquire();
        f.addListener((FutureListener<Channel>) f1 -> {
            if (f1.isSuccess()) {
                Channel ch = f1.getNow();
                ch.writeAndFlush(agentRequest);
                pool.release(ch);
            }
        });
        AgentResponse result = null;
        try {
            result = agentClientFuture.get(5000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            LOGGER.info("{} request timeout", agentRequest.getTraceId());
        }
        return result;
    }
}