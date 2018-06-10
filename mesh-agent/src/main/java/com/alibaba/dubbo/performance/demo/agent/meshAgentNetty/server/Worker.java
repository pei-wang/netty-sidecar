package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import java.util.concurrent.*;

public class Worker extends Thread {
    private static final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    public static ExecutorService executorService = new ThreadPoolExecutor(50, 80, 1000L, TimeUnit.MILLISECONDS, tasks);

    public static void dispatch(DBHandler dbHandler) {
        executorService.execute(dbHandler);
    }

}
