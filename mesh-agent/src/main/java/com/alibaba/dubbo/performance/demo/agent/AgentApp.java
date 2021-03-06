package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server.NettyServer;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentApp implements CommandLineRunner {
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(AgentApp.class);
        if ("provider".equals(System.getProperty("type"))) {
            springApplication.setWebEnvironment(false);
        }
        springApplication.run(args);
    }

    @Override
    public void run(String... strings) throws Exception {
        if ("provider".equals(System.getProperty("type"))) {
            IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
            new NettyServer().start();
        }
    }
}
