package com.net.server.util.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import io.netty.util.internal.SystemPropertyUtil;

public class NettyDefaultEventExecutorGroup extends NettyMultiThreadPoolExecutorGroup {
	static final int DEFAULT_MAX_PENDING_EXECUTOR_TASKS = Math.max(16,
            SystemPropertyUtil.getInt("io.netty.eventexecutor.maxPendingTasks", Integer.MAX_VALUE));
	
    public NettyDefaultEventExecutorGroup(int nThreads) {
        this(nThreads, null);
    }

    public NettyDefaultEventExecutorGroup(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, threadFactory, DEFAULT_MAX_PENDING_EXECUTOR_TASKS,RejectedExecutionHandlers.reject());
    }

    public NettyDefaultEventExecutorGroup(int nThreads, ThreadFactory threadFactory, int maxPendingTasks,
                                     RejectedExecutionHandler rejectedHandler) {
        super(nThreads, threadFactory, maxPendingTasks, rejectedHandler);
    }

    public EventExecutor next(int reqId){
    	return super.next(reqId);
    }
    @Override
    protected EventExecutor newChild(Executor executor, Object... args) throws Exception {
        return new DefaultEventExecutor(this, executor, (Integer) args[0], (RejectedExecutionHandler) args[1]);
    }
    
}
