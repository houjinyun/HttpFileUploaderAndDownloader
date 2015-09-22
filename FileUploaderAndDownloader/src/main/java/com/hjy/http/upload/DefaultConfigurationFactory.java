package com.hjy.http.upload;


import com.hjy.http.upload.parser.BaseResponseParser;
import com.hjy.http.upload.parser.StringResponseParser;
import com.hjy.http.upload.uploader.BaseUploader;
import com.hjy.http.upload.uploader.OKHttpUploader;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hjy on 7/8/15.<br>
 */
public class DefaultConfigurationFactory {

    /**
     * 创建线程池
     *
     * @param poolSize
     * @param threadPriority
     * @return
     */
    public static ExecutorService createExecutor(int poolSize, int threadPriority) {
        BlockingDeque<Runnable> taskQueue = new LinkedBlockingDeque<Runnable>();
        ThreadFactory threadFactory = creadDefaultThreadFactory(threadPriority);
        return new ThreadPoolExecutor(poolSize, poolSize, 30, TimeUnit.MILLISECONDS, taskQueue, threadFactory);
    }

    public static BaseUploader createDefaultUploader() {
        return new OKHttpUploader();
    }

    public static BaseResponseParser createDefaultResponseProcessor() {
        return new StringResponseParser();
    }

    private static DefaultThreadFactory creadDefaultThreadFactory(int priority) {
        return new DefaultThreadFactory(priority);
    }

    private static class DefaultThreadFactory implements ThreadFactory {

        private static final AtomicInteger THREAD_POOL_NUMBER = new AtomicInteger(1);

        private ThreadGroup threadGroup;
        private AtomicInteger threadNumber = new AtomicInteger(1);
        private int threadPriority;
        private String namePrefix;


        public DefaultThreadFactory(int priority) {
            this.threadPriority = priority;
            threadGroup = Thread.currentThread().getThreadGroup();
            namePrefix = "fileupload-" + THREAD_POOL_NUMBER.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(threadGroup, r, namePrefix + threadNumber.getAndIncrement());
            thread.setPriority(threadPriority);
            if(thread.isDaemon())
                thread.setDaemon(false);
            return thread;
        }
    }


}
