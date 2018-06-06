package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NettyClientFactory {
    private static IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private static List<Endpoint> endpoints;
    private static List<NettyClient> nettyClients = new ArrayList<>();
    private static Random random = new Random();

    static {
        try {
            endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
            for (Endpoint endpoint : endpoints) {
                NettyClient nettyClient = new NettyClient();
                nettyClient.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
                nettyClients.add(nettyClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NettyClient get() {
        return nettyClients.get(random.nextInt(nettyClients.size()));
    }

}