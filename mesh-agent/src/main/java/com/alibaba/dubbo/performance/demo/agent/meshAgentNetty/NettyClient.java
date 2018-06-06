package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
    public void connect(int port, String host) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(new ClientHandler());
                        }
                    });
            ChannelFuture f = b.connect(host, port).sync();
            for (int i = 0; i < 1000; i++) {
                byte[] req = "你好netty".getBytes();
                ByteBuf messageBuffer = Unpooled.buffer(req.length);
                messageBuffer.writeBytes(req);

                ChannelFuture channelFuture = f.channel().writeAndFlush(messageBuffer);
                channelFuture.syncUninterruptibly();
            }
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        try {
            new NettyClient().connect(10080, "127.0.0.1");
//            Channel channel = new AgentClientManager().getChannel("127.0.0.1", 10080);
//            byte[] req = "你好netty".getBytes();
//            ByteBuf messageBuffer = Unpooled.buffer(req.length);
//            messageBuffer.writeBytes(req);
//            ChannelFuture channelFuture = channel.writeAndFlush(messageBuffer);
//            channelFuture.syncUninterruptibly();
//            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
