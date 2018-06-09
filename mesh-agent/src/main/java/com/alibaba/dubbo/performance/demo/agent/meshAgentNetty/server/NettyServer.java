package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentDecoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentEncoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    int ioThreadNum = 15;
    //内核为此套接口排队的最大连接个数，对于给定的监听套接口，内核要维护两个队列，未链接队列和已连接队列大小总和最大值

    int backlog = 1024;

    int port = Integer.parseInt(System.getProperty("server.port"));

//    int port = 10080;
    public void start() throws InterruptedException {
        LOGGER.info("begin to start rpc server");
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(ioThreadNum);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, backlog)
                //注意是childOption
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                //真实数据最大字节数为Integer.MAX_VALUE，解码时自动去掉前面四个字节
                                //io.netty.handler.codec.DecoderException: java.lang.IndexOutOfBoundsException: readerIndex(900) + length(176) exceeds writerIndex(1024): UnpooledUnsafeDirectByteBuf(ridx: 900, widx: 1024, cap: 1024)
                                .addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                .addLast("encoder", new LengthFieldPrepender(4, false))
                                .addLast(new AgentDecoder(AgentRequest.class))
                                .addLast(new AgentEncoder(AgentResponse.class))
                                .addLast(new ServerHandler());
                    }
                });

        channel = serverBootstrap.bind(port).sync().channel();
        channel.closeFuture().sync();
        LOGGER.info("NettyRPC server listening on port " + port + " and ready for connections...");
    }
}

