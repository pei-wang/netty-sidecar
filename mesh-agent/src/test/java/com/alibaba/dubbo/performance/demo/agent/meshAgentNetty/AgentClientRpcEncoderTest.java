package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AgentClientRpcEncoderTest {
    @Mock
    private ChannelHandlerContext channelHandlerContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEncoder() throws Exception {
        AgentClientRpcEncoder agentClientRpcEncoder = new AgentClientRpcEncoder();
        AgentRequest agentRequest = new AgentRequest();
        agentRequest.setId(1);
        AgentRpcInvocation agentRpcInvocation = new AgentRpcInvocation();
        agentRpcInvocation.setInterfaceName("HelloService");
        agentRpcInvocation.setMethod("hello");
        agentRpcInvocation.setPrameterTypesString("String");
        agentRpcInvocation.setPrameter("pei");
        agentRequest.setmData(agentRpcInvocation);

        ByteBuf buffer = Unpooled.buffer();
        agentClientRpcEncoder.encode(channelHandlerContext,agentRequest, buffer);

        buffer.readerIndex(0);
        byte[] header = new byte[12];
        buffer.readBytes(header);
        byte[] dataLen = Arrays.copyOfRange(header,8,12);
        System.out.println(Bytes.bytes2int(dataLen));
        assertThat(Bytes.bytes2long(header,0),is(1L));
    }
}
