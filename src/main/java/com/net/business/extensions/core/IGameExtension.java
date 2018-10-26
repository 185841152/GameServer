package com.net.business.extensions.core;

import com.net.server.core.ISFSEventListener;
import com.net.server.core.SFSEventType;
import com.net.server.data.IGameObject;
import com.net.server.entities.User;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.List;

public abstract interface IGameExtension {
	public abstract void init();

	public abstract void destroy();

	public abstract String getName();

	public abstract void setName(String name);

	public abstract boolean isActive();

	public abstract void setActive(boolean active);

	public abstract void addEventListener(SFSEventType type, ISFSEventListener listener);

	public abstract void removeEventListener(SFSEventType type, ISFSEventListener listener);
	
	public abstract ScheduledFuture<?> addTask(int reqId,Runnable task,int time);

	public ScheduledFuture<?> addTask(int reqId,Runnable task,int delay,int reply);

	public abstract void handleClientRequest(Object cmd, User user, IGameObject params) throws Exception;

	public abstract void send(Object cmd, IGameObject params, User user, boolean isUdp);

	public abstract void send(Object cmd, IGameObject params, User user);

	public abstract void send(Object cmd, IGameObject params, List<User> users, boolean isUdp);

	public abstract void send(Object cmd, IGameObject params, List<User> users);
}