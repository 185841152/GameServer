package com.net.server.mmo;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.server.GameServer;
import com.net.server.api.IGameApi;
import com.net.server.entities.User;

class MMORoomCleaner implements Runnable {
	private final MMORoom targetRoom;
	private final IGameApi gameApi;
	private final Logger logger;
	private final long allowedTime;

	public MMORoomCleaner(MMORoom targetRoom) {
		this.targetRoom = targetRoom;
		this.gameApi = GameServer.getInstance().getAPIManager().getSFSApi();
		this.logger = LoggerFactory.getLogger(getClass());
		this.allowedTime = (targetRoom.getUserLimboMaxSeconds() * 1000);
	}

	public void run() {
		try {
			Collection<User> allUsers = this.targetRoom.getUserManager().getDirectUserList();

			for (User u : allUsers) {
				if (u.containsProperty("_uLoc")) {
					continue;
				}
				long joinTime = ((Long) u.getProperty("_uJoinTime")).longValue();

				if (System.currentTimeMillis() > joinTime + this.allowedTime)
					kickUserOut(u);
			}
		} catch (Exception e) {
		}
	}

	private void kickUserOut(User u) {
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("User: " + u + " kicked out of " + this.targetRoom);
		}
		this.gameApi.leaveRoom(u, this.targetRoom);
	}
}