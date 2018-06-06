package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AgentClientRpcDecoderTest {
    @Mock
    private ChannelHandlerContext channelHandlerContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDecode() throws Exception {
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
        agentClientRpcEncoder.encode(channelHandlerContext, agentRequest, buffer);

        List<Object> mList = new ArrayList<>();
        AgentClientRpcDecoder agentClientRpcDecoder = new AgentClientRpcDecoder();
        agentClientRpcDecoder.decode(channelHandlerContext, buffer, mList);

        assertThat(((AgentResponse) mList.get(0)).getRequestId(), is(1L));
    }
}
