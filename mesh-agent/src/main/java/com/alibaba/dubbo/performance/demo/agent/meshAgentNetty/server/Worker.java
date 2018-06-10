package com.alibaba.dubbo.performance.demo.agent.meshAgentNetty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Worker extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);
        private static final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
//    private static final SynchronousQueue<Runnable> tasks = new SynchronousQueue<>();

        public static ExecutorService executorService = new ThreadPoolExecutor(50, 80, 100L, TimeUnit.MILLISECONDS, tasks);
//    public static ExecutorService executorService = new ThreadPoolExecutor(80, Integer.MAX_VALUE, 100L, TimeUnit.MILLISECONDS, tasks);
    private static long startTime = System.currentTimeMillis();

    public static void dispatch(DBHandler dbHandler) {
        executorService.execute(dbHandler);
        LOGGER.info("current tasks size:" + tasks.size());
        if ((System.currentTimeMillis() - startTime) > 1000) {
            long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;//已使用内存
            long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;//总共可使用内存
            System.out.println("可用处理器：" + Runtime.getRuntime().availableProcessors());
            System.out.printf("可用内存:%sm ", freeMemory);
            System.out.printf("可用总内存:%sm \n", totalMemory);
        }
    }

}
