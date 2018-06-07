package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NettyClientFactory {
    private static Logger LOGGER = LoggerFactory.getLogger(NettyClientFactory.class);
    private List<Endpoint> endpoints;
    private List<NettyClient> nettyClients = new ArrayList<>();
    private static Random random = new Random();
    private static NettyClientFactory nettyClientFactory;

    private NettyClientFactory() {
        try {
            IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
            endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
            for (Endpoint endpoint : endpoints) {
                LOGGER.info("trying to connect endpoint{}:{}", endpoint.getHost(), endpoint.getPort());
                NettyClient nettyClient = new NettyClient();
                nettyClient.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
                LOGGER.info("connected to endpoint:{}:{}", endpoint.getHost(), endpoint.getPort());
                nettyClients.add(nettyClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NettyClient get() {
        if (nettyClientFactory == null) {
            nettyClientFactory = new NettyClientFactory();
        }
        return nettyClientFactory.nettyClients.get(random.nextInt(nettyClientFactory.nettyClients.size()));
    }

}