package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class ServerHandler extends SimpleChannelInboundHandler {

//    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
//
//    private RpcClient rpcClient = new RpcClient(registry);

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        AgentRequest agentRequest = (AgentRequest) msg;
//        ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) agentRequest.getmData());
//        ObjectInputStream objectInputStream = new ObjectInputStream(bis);
//        AgentRpcInvocation invocation = (AgentRpcInvocation) objectInputStream.readObject();
//        Object result = rpcClient.invoke(invocation.getInterfaceName(), invocation.getMethod(), invocation.getPrameterTypesString(), invocation.getPrameter());
        String[] dataReceived = new String((byte[]) agentRequest.getmData()).split(":");
        System.out.println(dataReceived[0] + dataReceived[1]+dataReceived[2]);
        Object result = "123".getBytes();
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setRequestId("" + agentRequest.getId());
        agentResponse.setBytes((byte[]) result);
        channelHandlerContext.write(agentResponse);
    }

//    public byte[] provider(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {
//
//        Object result = rpcClient.invoke(interfaceName, method, parameterTypesString, parameter);
//        return (byte[]) result;
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
