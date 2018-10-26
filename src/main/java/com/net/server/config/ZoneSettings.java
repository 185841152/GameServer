package com.net.server.config;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ZoneSettings {
	public static final String ENTITY_NAME = "ZONESETTINGS";

	public Integer id;
	public String name = "";
	public String serverName = "";
	public String node = "";
	public int maxUsers = 1000;
	public int maxUserVariablesAllowed = 5;
	public int maxRoomVariablesAllowed = 5;
	public int maxRooms = 500;
	public int userCountChangeUpdateInterval = 1000;
	public int userReconnectionSeconds = 300;
	public int overrideMaxUserIdleTime = 120;
	public int status = 0;

	public List<RoomSettings> rooms;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	public int getMaxUserVariablesAllowed() {
		return maxUserVariablesAllowed;
	}

	public void setMaxUserVariablesAllowed(int maxUserVariablesAllowed) {
		this.maxUserVariablesAllowed = maxUserVariablesAllowed;
	}

	public int getMaxRoomVariablesAllowed() {
		return maxRoomVariablesAllowed;
	}

	public void setMaxRoomVariablesAllowed(int maxRoomVariablesAllowed) {
		this.maxRoomVariablesAllowed = maxRoomVariablesAllowed;
	}

	public int getMaxRooms() {
		return maxRooms;
	}

	public void setMaxRooms(int maxRooms) {
		this.maxRooms = maxRooms;
	}

	public int getUserCountChangeUpdateInterval() {
		return userCountChangeUpdateInterval;
	}

	public void setUserCountChangeUpdateInterval(int userCountChangeUpdateInterval) {
		this.userCountChangeUpdateInterval = userCountChangeUpdateInterval;
	}

	public int getUserReconnectionSeconds() {
		return userReconnectionSeconds;
	}

	public void setUserReconnectionSeconds(int userReconnectionSeconds) {
		this.userReconnectionSeconds = userReconnectionSeconds;
	}

	public int getOverrideMaxUserIdleTime() {
		return overrideMaxUserIdleTime;
	}

	public void setOverrideMaxUserIdleTime(int overrideMaxUserIdleTime) {
		this.overrideMaxUserIdleTime = overrideMaxUserIdleTime;
	}

	public static final class RoomSettings {
		public static final String EVENTS = "USER_ENTER_EVENT,USER_EXIT_EVENT,USER_COUNT_CHANGE_EVENT,USER_VARIABLES_UPDATE_EVENT";
		private static final AtomicInteger idGenerator = new AtomicInteger();
		private transient Integer id;
		public String name = null;
		public String groupId = DefaultConstants.ZONE_DEFAULT_GROUP;
		public String password = null;
		public int maxUsers = 20;
		public int maxSpectators = 0;

		public boolean isDynamic = false;
		public boolean isGame = false;
		public boolean isHidden = false;

		public String autoRemoveMode = "DEFAULT";

		public String events = "USER_ENTER_EVENT,USER_EXIT_EVENT,USER_COUNT_CHANGE_EVENT,USER_VARIABLES_UPDATE_EVENT";

		public ZoneSettings.MMOSettings mmoSettings = new ZoneSettings.MMOSettings();

		public RoomSettings() {
			getId();
		}

		public RoomSettings(String name) {
			this();
			this.name = name;
			this.password = "";
		}

		public int getId() {
			if (this.id == null) {
				this.id = Integer.valueOf(getUniqueId());
			}
			return this.id.intValue();
		}

		private static int getUniqueId() {
			return idGenerator.getAndIncrement();
		}

		public String getAvailableEvents() {
			return "USER_ENTER_EVENT,USER_EXIT_EVENT,USER_COUNT_CHANGE_EVENT,USER_VARIABLES_UPDATE_EVENT";
		}
	}

	public static final class MMOSettings implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4809915500328735544L;
		public boolean isActive = false;
		public String defaultAOI = "100,100,0";
		public String lowerMapLimit = "";
		public String higherMapLimit = "";
		public boolean forceFloats = false;
		public int userMaxLimboSeconds = 50;
		public int proximityListUpdateMillis = 500;
		public boolean sendAOIEntryPoint = true;
	}

}