package com.happylee.mydemo.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 李俊德(Jed Li)
 */
public class ThreadPoolSingleton {
    private ExecutorService executorService;
    /**获得Java虚拟机中可用的处理器数量*/
    private final int availableProcessor = Runtime.getRuntime().availableProcessors();
    /**参数配置来源于Github安卓开发者DevYK*/
    private final int corePoolSize = availableProcessor + 1;
    private final int maximumPoolSize = availableProcessor * 2 + 1;
    private final int keepAliveTime = 3;
    private final int blockingQueueSize = 128;

    private volatile static ThreadPoolSingleton instance;
    /**在私有构造方法中对ExecutorService进行初始化*/
    private ThreadPoolSingleton() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                    keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(blockingQueueSize), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return null;
                }
            }, new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    //在此编写线程池拒绝策略的逻辑代码
                }
            });
        }
    }

    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    public static ThreadPoolSingleton getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolSingleton.class) {
                if (instance == null) {
                    instance = new ThreadPoolSingleton();
                }
            }
        }
        return instance;
    }
}
