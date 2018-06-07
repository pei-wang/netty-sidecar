package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.client;

import com.alibaba.dubbo.performance.demo.agent.AgentClientFuture;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentDecoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentEncoder;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common.AgentResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private Channel channel;
    private volatile boolean closed = false;
    int workerGroupThreads = 5;
    private ClientHandler clientHandler = new ClientHandler();

    public void connect(final InetSocketAddress socketAddress) {
        try{
            workerGroup = new NioEventLoopGroup(workerGroupThreads);
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    //处理失败重连
                                    .addFirst(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            super.channelInactive(ctx);
                                            ctx.channel().eventLoop().schedule(new Runnable() {
                                                @Override
                                                public void run() {
                                                    doConnect(socketAddress);
                                                }
                                            },1, TimeUnit.SECONDS);
                                        }
                                    })
                                    //处理分包传输问题
                                    .addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                    .addLast("encoder", new LengthFieldPrepender(4, false))
                                    .addLast(new AgentDecoder(AgentResponse.class))
                                    .addLast(new AgentEncoder(AgentRequest.class))
                                    .addLast(clientHandler);
                        }
                    });
            doConnect(socketAddress);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
    }

    private void doConnect(final InetSocketAddress socketAddress) {
        logger.info("trying to connect server:{}",socketAddress);
        if (closed) {
            return;
        }

        ChannelFuture future = bootstrap.connect(socketAddress);
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    logger.info("connected to {}", socketAddress);
                } else {
                    logger.info("connected to {} failed",socketAddress);
                    f.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect(socketAddress);
                        }
                    }, 1, TimeUnit.SECONDS);
                }
            }
        });

        channel = future.syncUninterruptibly()
                .channel();
    }

    public InetSocketAddress getRemoteAddress() {
        SocketAddress remoteAddress = channel.remoteAddress();
        if (!(remoteAddress instanceof InetSocketAddress)) {
            throw new RuntimeException("Get remote address error, should be InetSocketAddress");
        }
        return (InetSocketAddress) remoteAddress;
    }

    public boolean isClosed() {
        return closed;
    }

    @PreDestroy
    public void close() {
        logger.info("destroy client resources");
        if (null == channel) {
            logger.error("channel is null");
        }
        closed = true;
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
        workerGroup = null;
        channel = null;
    }

    public AgentResponse sendData(AgentRequest agentRequest) {
        AgentClientFuture agentClientFuture = new AgentClientFuture();
        AgentClientRequestHolder.put(String.valueOf(agentRequest.getTraceId()), agentClientFuture);
        channel.writeAndFlush(agentRequest);
        AgentResponse result = null;
        try {
            result = agentClientFuture.get(5000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            logger.info("{} request timeout", agentRequest.getTraceId());
        }
        return result;
    }
}