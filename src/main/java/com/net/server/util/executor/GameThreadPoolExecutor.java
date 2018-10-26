package com.net.server.util.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GameThreadPoolExecutor extends ThreadPoolExecutor
{
	
    private final Logger logger = LoggerFactory.getLogger(ExecutorConfig.class);
    private final ExecutorConfig cfg;
    private final int maxThreads;
    private final int backupThreadsExpirySeconds;
    private volatile long lastQueueCheckTime;
    private volatile long lastBackupTime;
    private volatile boolean threadShutDownNotified;
    
    public GameThreadPoolExecutor(ExecutorConfig config){
        super(config.coreThreads, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new MyThreadFactory(config.name));
        threadShutDownNotified = false;
        cfg = config;
        maxThreads = cfg.coreThreads + cfg.backupThreads * cfg.maxBackups;
        backupThreadsExpirySeconds = cfg.backupThreadsExpiry * 1000;
        lastQueueCheckTime = -1L;
    }
    
    public void execute(Runnable command){
        if(getPoolSize() >= cfg.coreThreads){
            boolean needsBackup = checkQueueWarningLevel();
            if(needsBackup){
                if(getPoolSize() >= maxThreads){
                    logger.warn(String.format("队列数已经大于: %s, 备用线程已经启用: %s", new Object[] {
                        Integer.valueOf(getQueue().size()), Integer.valueOf(getPoolSize())
                    }));
                } else{
                    setCorePoolSize(getPoolSize() + cfg.backupThreads);
                    lastBackupTime = lastQueueCheckTime = System.currentTimeMillis();
                    threadShutDownNotified = false;
                    logger.info(String.format("新增 %s 新线程, 当前线程数量: %s", new Object[] {
                        Integer.valueOf(cfg.backupThreads), Integer.valueOf(getPoolSize())
                    }));
                }
            } else if(getPoolSize() > cfg.coreThreads){
                boolean isTimeToShutDownBackupThreads = System.currentTimeMillis() - lastBackupTime > (long)backupThreadsExpirySeconds;
                boolean isQueueSizeSmallEnough = getQueue().size() < cfg.queueSizeTriggeringBackupExpiry; 
                if(isTimeToShutDownBackupThreads && isQueueSizeSmallEnough && !threadShutDownNotified){
                    setCorePoolSize(cfg.coreThreads);
                    threadShutDownNotified = true;
                    logger.info("关闭备用线程");
                }
            }
        }
        super.execute(command);
    }

    private boolean checkQueueWarningLevel()
    {
        boolean needsBackup = false;
        boolean queueIsBusy = getQueue().size() >= cfg.queueSizeTriggeringBackup;
        long now = System.currentTimeMillis();
        if(lastQueueCheckTime < 0L)
            lastQueueCheckTime = now;
        if(queueIsBusy)
        {
            if(now - lastQueueCheckTime > (long)(cfg.secondsTriggeringBackup * 1000))
                needsBackup = true;
        } else
        {
            lastQueueCheckTime = now;
        }
        return needsBackup;
    }
    
    
    private static final class MyThreadFactory implements ThreadFactory{
    	
    	private static final AtomicInteger POOL_ID = new AtomicInteger(0);
        private final AtomicInteger threadId = new AtomicInteger(1);
        private final String poolName;
        
        public Thread newThread(Runnable r){
            Thread t = new Thread(r, String.format("Worker:%s:%s", new Object[] {
                poolName == null ? Integer.valueOf(POOL_ID.get()) : poolName, Integer.valueOf(threadId.getAndIncrement())
            }));
            if(t.isDaemon())
                t.setDaemon(false);
            if(t.getPriority() != 5)
                t.setPriority(5);
            return t;
        }
        public MyThreadFactory(String poolName){
            this.poolName = poolName;
            POOL_ID.incrementAndGet();
        }
    }

    public int getCoreThreads(){
        return cfg.coreThreads;
    }

    public int getBackupThreads(){
        return cfg.backupThreads;
    }

    public int getMaxBackups(){
        return cfg.maxBackups;
    }

    public int getQueueSizeTriggeringBackup(){
        return cfg.queueSizeTriggeringBackup;
    }

    public int getSecondsTriggeringBackup(){
        return cfg.secondsTriggeringBackup;
    }

    public int getBackupThreadsExpiry(){
        return cfg.backupThreadsExpiry;
    }

    public int getQueueSizeTriggeringBackupExpiry(){
        return cfg.queueSizeTriggeringBackupExpiry;
    }

}