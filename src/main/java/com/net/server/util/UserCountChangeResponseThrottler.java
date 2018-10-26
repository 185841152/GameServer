package com.net.server.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.io.IResponse;
import com.net.server.GameServer;
import com.net.server.data.IGameObject;
import com.net.server.exceptions.SFSRuntimeException;

public final class UserCountChangeResponseThrottler implements IResponseThrottler {
	private static final int MIN_INTERVAL_MILLIS = 250;
	private Runnable taskHandler;
	private final Map<Integer, IResponse> responsesByRoomId;
	private volatile int interval;
	private final Logger log;
	private final String zoneName;
	private final GameServer server;

	public UserCountChangeResponseThrottler(int delay, String zoneName) {
		this.log = LoggerFactory.getLogger(getClass());
		this.responsesByRoomId = new ConcurrentHashMap<Integer, IResponse>();
		this.zoneName = zoneName;
		this.server = GameServer.getInstance();

		setInterval(delay);
	}

	public String getName() {
		return this.zoneName;
	}

	public int getInterval() {
		return this.interval;
	}

	public void setInterval(int delay) {
		this.interval = (delay >= MIN_INTERVAL_MILLIS ? delay : 0);

		if (this.taskHandler != null) {
			((UCountTaskHandler) this.taskHandler).stop();
		}

		if (this.interval > 0) {
			this.taskHandler = new UCountTaskHandler();

			this.server.getTaskScheduler().scheduleAtFixedRate(this.taskHandler, 0, this.interval,
					TimeUnit.MILLISECONDS);
		}
	}

	public void enqueueResponse(Object o) {
		IResponse response = (IResponse) o;

		if (this.interval == 0) {
			response.write();
		} else {
			IGameObject sfso = (IGameObject) response.getContent();
			Integer roomId = sfso.getInt("r");

			if (roomId == null) {
				throw new SFSRuntimeException(
						"Unexpected malformed UCount response, missing room id:\n " + sfso.getDump());
			}

			this.responsesByRoomId.put(roomId, response);
		}
	}

	final class UCountTaskHandler implements Runnable {
		private volatile boolean stopMe = false;

		UCountTaskHandler() {
		}

		public void run() {
			if (this.stopMe) {
				throw new RuntimeException("Stopping");
			}

			try {
				for (Iterator<IResponse> iter = UserCountChangeResponseThrottler.this.responsesByRoomId.values()
						.iterator(); iter.hasNext();) {
					IResponse response = (IResponse) iter.next();
					UserCountChangeResponseThrottler.this.log.debug("---> Throttler running: " + response);

					response.write();
					iter.remove();
				}
			} catch (Exception err) {
				UserCountChangeResponseThrottler.this.log.warn("Unexpected Error: " + err);
			}
		}

		public void stop() {
			this.stopMe = true;
		}
	}
}