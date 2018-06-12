package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static Logger LOGGER = LoggerFactory.getLogger(RpcClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        LOGGER.info("Request-traceId:{} get response from dubbo{}", rpcResponse.getRequestId(), new String(rpcResponse.getBytes()));
        RpcClient.getInstance().handleResponse(rpcResponse);
    }
}
