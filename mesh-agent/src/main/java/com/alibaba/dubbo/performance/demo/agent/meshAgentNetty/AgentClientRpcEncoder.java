package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class AgentClientRpcEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        AgentRequest agentRequest = (AgentRequest) o;
        byte[] header = new byte[12];
        Bytes.long2bytes(agentRequest.getId(), header, 0);

        AgentRpcInvocation invocation = (AgentRpcInvocation) agentRequest.getmData();
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(bos));
//        JsonUtils.writeObject(invocation.getInterfaceName(), printWriter);
//        JsonUtils.writeObject(invocation.getMethod(), printWriter);
//        JsonUtils.writeObject(invocation.getPrameterTypesString(), printWriter);
//        JsonUtils.writeObject(invocation.getPrameter(), printWriter);
//        ObjectOutputStream outputStream = new ObjectOutputStream(bos);
//        outputStream.writeObject(invocation);
        byte[] requestData = (invocation.getInterfaceName() + ":" + invocation.getMethod() + ":" + invocation.getPrameterTypesString() + ":" + invocation.getPrameter()).getBytes();
        int len = requestData.length;
        byteBuf.writerIndex(12);
        byteBuf.writeBytes(requestData);
        Bytes.int2bytes(len, header, 8);


        byteBuf.writerIndex(0);
        byteBuf.writeBytes(header);
        byteBuf.writerIndex(12 + len);
    }
}
