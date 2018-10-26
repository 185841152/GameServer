package com.net.engine.core;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.data.IPacket;
import com.net.engine.data.Packet;
import com.net.engine.events.Event;
import com.net.engine.events.IEvent;
import com.net.engine.exceptions.MessageQueueFullException;
import com.net.engine.exceptions.PacketQueueWarning;
import com.net.engine.io.IResponse;
import com.net.engine.io.protocols.ProtocolType;
import com.net.engine.service.BaseCoreService;
import com.net.engine.sessions.IPacketQueue;
import com.net.engine.sessions.ISession;
import com.net.engine.sessions.SessionType;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;

import io.netty.channel.Channel;

public final class SocketWriter extends BaseCoreService implements ISocketWriter, Runnable {
	private final Logger logger;
	private final Logger bootLogger;
	private final ExecutorService threadPool;
	private final BlockingQueue<ISession> sessionTicketsQueue;
	private volatile int threadId = 1;
	private volatile boolean isActive = false;
	private volatile long droppedPacketsCount = 0L;
	private volatile long writtenBytes = 0L;
	private volatile long writtenPackets = 0L;
	private volatile long droppedUdpPacketsCount = 0L;
	private int threadPoolSize;

	public SocketWriter(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;

		this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
		this.logger = LoggerFactory.getLogger(SocketWriter.class);
		this.bootLogger = LoggerFactory.getLogger("bootLogger");

		this.sessionTicketsQueue = new LinkedBlockingQueue<ISession>();
	}

	public void init(Object o) {
		super.init(o);

		if (this.isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		if (this.threadPoolSize < 1) {
			throw new IllegalArgumentException("Illegal value for a thread pool size: " + this.threadPoolSize);
		}
 
		this.isActive = true;

		initThreadPool();

		this.bootLogger.info("Socket Writer started (pool size:" + this.threadPoolSize + ")");
	}

	public void destroy(Object o) {
		super.destroy(o);

		this.isActive = false;
		List<Runnable> leftOvers = this.threadPool.shutdownNow();
		this.bootLogger.info("SocketWriter stopped. Unprocessed tasks: " + leftOvers.size());
	}

	public int getQueueSize() {
		return this.sessionTicketsQueue.size();
	}

	public int getThreadPoolSize() {
		return this.threadPoolSize;
	}

	public void continueWriteOp(ISession session) {
		if (session != null)
			this.sessionTicketsQueue.add(session);
	}

	private void initThreadPool() {
		for (int j = 0; j < this.threadPoolSize; j++)
			this.threadPool.execute(this);
	}

	public void run() {
		Thread.currentThread().setName("SocketWriter-" + this.threadId++);

		while (this.isActive) {
			try {
				ISession session = (ISession) this.sessionTicketsQueue.take();

				processSessionQueue(session);
			} catch (InterruptedException e) {
				this.logger.warn("SocketWriter thread interrupted: " + Thread.currentThread());
				this.isActive = false;
			} catch (Throwable t) {
				this.logger.warn("Problems in SocketWriter main loop, Thread: " + Thread.currentThread());
			}
		}

		this.bootLogger.info("SocketWriter threadpool shutting down.");
	}

	private void processSessionQueue(ISession session) {
		if (session != null) {
			SessionType type = session.getType();

			if (type == SessionType.DEFAULT) {
				processRegularSession(session);
			} else if (type == SessionType.VOID)
				return;
		}
	}

	private void processRegularSession(ISession session) {
		if (session.isFrozen()) {
			return;
		}
		IPacket packet = null;
		try {
			IPacketQueue sessionQ = session.getPacketQueue();

			synchronized (sessionQ) {
				if (!sessionQ.isEmpty()) {
					packet = sessionQ.peek();

					if (packet == null) {
						return;
					}

					if (packet.isTcp()) {
						tcpSend(sessionQ, session, packet);
					} else if (packet.isUdp()) {
						udpSend(sessionQ, session, packet);
					} else {
						this.logger.warn("Unknow packet type: " + packet);
					}

				}

			}
		} catch (ClosedChannelException cce) {
			this.logger.debug("Socket closed during write operation for session: " + session);
		} catch (IOException localIOException) {
		} catch (Exception e) {
			this.logger.warn("Error during write. Session: " + session);
		}
	}

	private void tcpSend(IPacketQueue sessionQ, ISession session, IPacket packet) throws Exception {
		Channel channel = session.getConnection();

		if (channel == null) {
			this.logger.debug("Skipping packet, found null socket for Session: " + session);
			return;
		}
		channel.writeAndFlush(packet);

		sessionQ.take();

		if (!sessionQ.isEmpty()) {
			this.sessionTicketsQueue.add(session);
		}
	}

	private void udpSend(IPacketQueue sessionQ, ISession session, IPacket packet) throws Exception {

	}

	public void onPacketWrite(IResponse response) {
		IGameObject params = GameObject.newInstance();

		params.putShort("a", ((Short) response.getId()).shortValue());

		IGameObject data=(IGameObject) response.getContent();
		if (response.getUserIds()!=null) {
			data.putIntArray("us", response.getUserIds());
		}
		
		params.putGameObject("p", data);
		

		IPacket packet = new Packet();
		packet.setTransportType(response.getTransportType());
		packet.setData(params);
		packet.setRecipients(response.getRecipients());
		packet.setProtocolType(ProtocolType.BINARY);

		enqueuePacket(packet);
	}

	public void enqueuePacket(IPacket packet) {
		enqueueLocal(packet);
	}

	private void enqueueLocal(IPacket packet) {
		ISession recipients = packet.getRecipients();
		enqueueLocalPacket(recipients, packet.clone());
	}

	private void enqueueLocalPacket(ISession session, IPacket packet) {
		IPacketQueue sessionQ = session.getPacketQueue();

		if (sessionQ != null) {
			synchronized (sessionQ) {
				try {
					boolean wasEmpty = sessionQ.isEmpty();

					sessionQ.put(packet);

					if (wasEmpty) {
						this.sessionTicketsQueue.add(session);
					}

					packet.setRecipients(null);
				} catch (PacketQueueWarning err) {
					dropOneMessage(session);

					if (this.logger.isDebugEnabled()) {
						this.logger.debug(err.getMessage() + ": " + session);
					}

				} catch (MessageQueueFullException error) {
					dropOneMessage(session);
				}
			}
		}
	}

	private void dropOneMessage(ISession session) {
		session.addDroppedMessages(1);
		this.droppedPacketsCount += 1L;

		IEvent event = new Event("packetDropped");
		event.setParameter("session", session);
		dispatchEvent(event);
	}

	public long getDroppedPacketsCount() {
		return this.droppedPacketsCount;
	}

	public long getDroppedUdpPacketCount() {
		return this.droppedUdpPacketsCount;
	}

	public long getWrittenBytes() {
		return this.writtenBytes;
	}

	public long getWrittenPackets() {
		return this.writtenPackets;
	}

}