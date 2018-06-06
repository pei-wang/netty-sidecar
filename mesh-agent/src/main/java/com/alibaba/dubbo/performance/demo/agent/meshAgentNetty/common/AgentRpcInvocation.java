package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.common;

import java.io.Serializable;

public class AgentRpcInvocation implements Serializable {
    private String interfaceName;
    private String method;
    private String prameterTypesString;
    private String prameter;

    public AgentRpcInvocation() {
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPrameterTypesString() {
        return prameterTypesString;
    }

    public void setPrameterTypesString(String prameterTypesString) {
        this.prameterTypesString = prameterTypesString;
    }

    public String getPrameter() {
        return prameter;
    }

    public void setPrameter(String prameter) {
        this.prameter = prameter;
    }
}
