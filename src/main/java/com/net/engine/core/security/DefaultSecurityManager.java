package com.net.engine.core.security;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultSecurityManager implements ISecurityManager {
	private final List<IAllowedThread> secureThreads;
	private String name;
	private final Logger bootLogger;

	public DefaultSecurityManager() {
		this.secureThreads = new ArrayList<IAllowedThread>();
		this.bootLogger = LoggerFactory.getLogger("bootLogger");
	}

	public void init(Object o) {
		this.secureThreads
				.add(new EngineThread("com.smartfoxserver.bitswarm.controllers", ThreadComparisonType.STARTSWITH));

		this.bootLogger.info("Security Manager started");
	}

	public void destroy(Object o) {
		this.bootLogger.info("Security Manager stopped");
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void handleMessage(Object message) {
	}

	public boolean isEngineThread(Thread thread) {
		boolean okay = false;
		String currThreadName = thread.getName();

		for (IAllowedThread allowedThread : this.secureThreads) {
			if (allowedThread.getComparisonType() == ThreadComparisonType.STARTSWITH) {
				if (!currThreadName.startsWith(allowedThread.getName()))
					continue;
				okay = true;
				break;
			}

			if (allowedThread.getComparisonType() == ThreadComparisonType.EXACT) {
				if (!currThreadName.equals(currThreadName))
					continue;
				okay = true;
				break;
			}

			if (allowedThread.getComparisonType() != ThreadComparisonType.ENDSWITH)
				continue;
			if (!currThreadName.endsWith(allowedThread.getName()))
				continue;
			okay = true;
			break;
		}

		return okay;
	}
}