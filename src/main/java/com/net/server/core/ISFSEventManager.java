package com.net.server.core;

import com.net.engine.service.IService;
import com.net.server.util.executor.NettyDefaultEventExecutorGroup;

public abstract interface ISFSEventManager extends ISFSEventDispatcher, IService {
	public abstract NettyDefaultEventExecutorGroup getThreadPool();
}