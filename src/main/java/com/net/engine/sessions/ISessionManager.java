package com.net.engine.sessions;

import java.io.IOException;
import java.util.List;

import com.net.engine.exceptions.SessionReconnectionException;
import com.net.engine.service.IService;

import io.netty.channel.Channel;

public abstract interface ISessionManager extends IService {
	public abstract void addSession(ISession paramISession);

	public abstract void removeSession(ISession paramISession);

	public abstract ISession removeSession(int paramInt);

	public abstract ISession removeSession(String paramString);

	public abstract ISession removeSession(Channel paramSocketChannel);

	public abstract boolean containsSession(ISession paramISession);

	public abstract void shutDownLocalSessions();

	public abstract List<ISession> getAllSessions();

	public abstract ISession getSessionById(int paramInt);

	public abstract ISession getSessionByHash(String paramString);

	public abstract int getNodeSessionCount(String paramString);

	public abstract int getHighestCCS();

	public abstract List<ISession> getAllSessionsAtNode(String paramString);

	public abstract List<ISession> getAllLocalSessions();

	public abstract ISession getLocalSessionById(int paramInt);

	public abstract ISession getLocalSessionByHash(String paramString);

	public abstract ISession getLocalSessionByConnection(Channel paramSocketChannel);

	public abstract int getLocalSessionCount();

	public abstract ISession createSession(Channel paramSocketChannel);

	public abstract ISession createConnectionlessSession();

	public abstract ISession createWebSocketSession(Object paramObject);

	public abstract void publishLocalNode(String paramString);

	public abstract void clearClusterData();

	public abstract void onNodeLost(String paramString);

	public abstract void onSocketDisconnected(Channel paramSocketChannel) throws IOException;

	public abstract void onSocketDisconnected(ISession paramISession) throws IOException;

	public abstract ISession reconnectSession(ISession paramISession, String paramString)
			throws SessionReconnectionException, IOException;
}