package com.net.server.core;

import com.net.engine.service.IService;

public abstract interface ICoreService extends IService {
	public abstract boolean isActive();
}