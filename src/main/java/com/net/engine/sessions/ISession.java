package com.net.engine.sessions;

import java.io.IOException;

import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramChannel;

public abstract interface ISession {
	public abstract int getId();

	public abstract void setId(int paramInt);

	public abstract String getHashId();

	public abstract void setHashId(String paramString);

	public abstract SessionType getType();

	public abstract void setType(SessionType paramSessionType);

	public abstract String getNodeId();

	public abstract void setNodeId(String paramString);

	public abstract boolean isLocal();

	public abstract boolean isLoggedIn();

	public abstract void setLoggedIn(boolean paramBoolean);

	public abstract IPacketQueue getPacketQueue();

	public abstract void setPacketQueue(IPacketQueue paramIPacketQueue);

	public abstract Channel getConnection();

	public abstract void setConnection(Channel paramSocketChannel);

	public abstract DatagramChannel getDatagramChannel();

	public abstract void setDatagrmChannel(DatagramChannel paramDatagramChannel);

	public abstract long getCreationTime();

	public abstract void setCreationTime(long paramLong);

	public abstract boolean isConnected();

	public abstract void setConnected(boolean paramBoolean);

	public abstract long getLastActivityTime();

	public abstract void setLastActivityTime(long paramLong);

	public abstract long getLastLoggedInActivityTime();

	public abstract void setLastLoggedInActivityTime(long paramLong);

	public abstract long getLastReadTime();

	public abstract void setLastReadTime(long paramLong);

	public abstract long getLastWriteTime();

	public abstract void setLastWriteTime(long paramLong);

	public abstract long getReadBytes();

	public abstract void addReadBytes(long paramLong);

	public abstract long getWrittenBytes();

	public abstract void addWrittenBytes(long paramLong);

	public abstract int getDroppedMessages();

	public abstract void addDroppedMessages(int paramInt);

	public abstract int getMaxIdleTime();

	public abstract void setMaxIdleTime(int paramInt);

	public abstract int getMaxLoggedInIdleTime();

	public abstract void setMaxLoggedInIdleTime(int paramInt);

	public abstract boolean isMarkedForEviction();

	public abstract void setMarkedForEviction();

	public abstract boolean isIdle();

	public abstract boolean isFrozen();

	public abstract void freeze();

	public abstract void unfreeze();

	public abstract long getFreezeTime();

	public abstract boolean isReconnectionTimeExpired();

	public abstract Object getSystemProperty(String paramString);

	public abstract void setSystemProperty(String paramString, Object paramObject);

	public abstract void removeSystemProperty(String paramString);

	public abstract Object getProperty(String paramString);

	public abstract void setProperty(String paramString, Object paramObject);

	public abstract void removeProperty(String paramString);

	public abstract String getFullIpAddress();

	public abstract String getAddress();

	public abstract int getClientPort();

	public abstract String getServerAddress();

	public abstract int getServerPort();

	public abstract String getFullServerIpAddress();

	public abstract ISessionManager getSessionManager();

	public abstract void setSessionManager(ISessionManager paramISessionManager);

	public abstract void close() throws IOException;

	public abstract int getReconnectionSeconds();

	public abstract void setReconnectionSeconds(int paramInt);
}