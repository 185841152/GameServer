package com.net.engine.core.security;

import com.net.engine.service.IService;

public abstract interface ISecurityManager extends IService {
	public abstract boolean isEngineThread(Thread paramThread);
}