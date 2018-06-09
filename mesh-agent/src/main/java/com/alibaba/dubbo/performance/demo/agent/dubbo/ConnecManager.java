package com.alibaba.dubbo.performance.demo.agent.dubbo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.net.InetSocketAddress;

public class ConnecManager {
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private Bootstrap bootstrap;
    private SimpleChannelPool simpleChannelPool;

    public ConnecManager() {
        initBootstrap();
        int port = Integer.valueOf(System.getProperty("dubbo.protocol.port"));
        InetSocketAddress key = new InetSocketAddress("127.0.0.1", port);
        simpleChannelPool = new FixedChannelPool(bootstrap.remoteAddress(key), new RpcChannelPoolHandler(), 4);
        for (int i = 0; i < 4; i++) {
            Future<Channel> f = simpleChannelPool.acquire();
            f.addListener((FutureListener<Channel>) f1 -> {
                if (f1.isSuccess()) {
                    Channel ch = f1.getNow();
                    simpleChannelPool.release(ch);
                }
            });
        }
    }

    public SimpleChannelPool getChannel() throws Exception {
        return simpleChannelPool;
    }

    public void initBootstrap() {

        bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class);
    }
}
