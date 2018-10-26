package com.net.server.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.server.data.GameArray;
import com.net.server.data.IGameArray;
import com.net.server.entities.variables.UserVariable;
import com.net.server.entities.variables.VariableType;
import com.net.server.exceptions.SFSVariableException;
import com.net.server.mmo.BaseMMOItem;
import com.net.server.mmo.MMORoom;

public class GameUser implements User {
	private int id;
	private int sessionId;
	private String ip;
	private String name;
	private short privilegeId = 0;
	private String ipAddress = "";
	private Zone currentZone;
	private volatile long lastLoginTime = 0L;
	private final Set<String> registeredGroups;
	private final LinkedList<Room> joinedRooms;
	private final Set<Room> createdRooms;
	private final ConcurrentMap<Integer, Integer> playerIdByRoomId;
	private final ConcurrentMap<Object, Object> properties;
	private final ConcurrentMap<String, UserVariable> variables;
	private volatile int ownedRoomsCount = 0;
	private volatile int badWordsWarnings = 0;
	private volatile int floodWarnings = 0;
	private volatile boolean beingKicked = false;
	private volatile boolean connected = false;
	private boolean joining = false;
	private int maxVariablesAllowed = 0;
	private Logger logger;
	private volatile List<User> proxyList;
	private volatile List<BaseMMOItem> mmoItemsList;
	private volatile MMORoom lastJoinedMMORoom;
	private int status;

	public GameUser(Integer id, String name) {
		this.id = id;
		this.name = name;
		this.beingKicked = false;

		this.joinedRooms = new LinkedList<Room>();
		this.properties = new ConcurrentHashMap<Object, Object>();
		this.playerIdByRoomId = new ConcurrentHashMap<Integer, Integer>();
		this.variables = new ConcurrentHashMap<String, UserVariable>();
		this.registeredGroups = new HashSet<String>();
		this.createdRooms = new HashSet<Room>();

		this.logger = LoggerFactory.getLogger(getClass());
	}

	public int getId() {
		return this.id;
	}

	public short getPrivilegeId() {
		return this.privilegeId;
	}

	public void setPrivilegeId(short id) {
		this.privilegeId = id;
	}

	public boolean isConnected() {
		return this.connected;
	}

	public synchronized void setConnected(boolean flag) {
		this.connected = flag;
	}

	public synchronized boolean isJoining() {
		return this.joining;
	}

	public synchronized void setJoining(boolean flag) {
		this.joining = flag;
	}

	public int getMaxAllowedVariables() {
		return this.maxVariablesAllowed;
	}

	public synchronized void setMaxAllowedVariables(int max) {
		this.maxVariablesAllowed = max;
	}

	public void addCreatedRoom(Room room) {
		synchronized (this.createdRooms) {
			this.createdRooms.add(room);
		}
	}

	public List<Room> getCreatedRooms() {
		List<Room> rooms = null;

		synchronized (this.createdRooms) {
			rooms = new ArrayList<Room>(this.createdRooms);
		}

		return rooms;
	}

	public void removeCreatedRoom(Room room) {
		synchronized (this.createdRooms) {
			this.createdRooms.remove(room);
		}
	}

	public void addJoinedRoom(Room room) {
		synchronized (this.joinedRooms) {
			if (!this.joinedRooms.contains(room))
				this.joinedRooms.add(room);
		}
	}

	public void removeJoinedRoom(Room room) {
		synchronized (this.joinedRooms) {
			this.joinedRooms.remove(room);
		}

		this.playerIdByRoomId.remove(Integer.valueOf(room.getId()));
	}

	public int getOwnedRoomsCount() {
		return this.ownedRoomsCount;
	}

	public void subscribeGroup(String id) {
		synchronized (this.registeredGroups) {
			this.registeredGroups.add(id);
		}
	}

	public void unsubscribeGroup(String id) {
		synchronized (this.registeredGroups) {
			this.registeredGroups.remove(id);
		}
	}

	public List<String> getSubscribedGroups() {
		List<String> theGroups = null;

		synchronized (this.registeredGroups) {
			theGroups = new ArrayList<String>(this.registeredGroups);
		}

		return theGroups;
	}

	public boolean isSubscribedToGroup(String id) {
		boolean found = false;

		synchronized (this.registeredGroups) {
			found = this.registeredGroups.contains(id);
		}

		return found;
	}

	public List<Room> getJoinedRooms() {
		List<Room> rooms;
		synchronized (this.joinedRooms) {
			rooms = new ArrayList<Room>(this.joinedRooms);
		}
		return rooms;
	}

	public Room getLastJoinedRoom() {
		Room lastRoom = null;

		synchronized (this.joinedRooms) {
			if (this.joinedRooms.size() > 0) {
				lastRoom = (Room) this.joinedRooms.getLast();
			}
		}
		return lastRoom;
	}

	public boolean isJoinedInRoom(Room room) {
		boolean found = false;

		synchronized (this.joinedRooms) {
			found = this.joinedRooms.contains(room);
		}

		return found;
	}

	public long getLoginTime() {
		return this.lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPlayerId() {
		Room theRoom = getLastJoinedRoom();

		if (theRoom == null) {
			return 0;
		}
		return ((Integer) this.playerIdByRoomId.get(Integer.valueOf(theRoom.getId()))).intValue();
	}

	public int getPlayerId(Room room) {
		if (room == null) {
			return 0;
		}
		Integer playerId = (Integer) this.playerIdByRoomId.get(Integer.valueOf(room.getId()));

		if (playerId == null) {
			this.logger.info(
					"Can't find playerID -- User: " + this.name + " is not joined in the requested Room: " + room);
			playerId = Integer.valueOf(0);
		}

		return playerId.intValue();
	}

	public Map<Room, Integer> getPlayerIds() {
		Map<Room, Integer> allPlayerIds = new HashMap<Room, Integer>();

		synchronized (this.joinedRooms) {
			for (Room room : this.joinedRooms) {
				allPlayerIds.put(room, Integer.valueOf(getPlayerId(room)));
			}
		}
		return allPlayerIds;
	}

	public void setPlayerId(int id, Room room) {
		this.playerIdByRoomId.put(Integer.valueOf(room.getId()), Integer.valueOf(id));
	}

	public boolean isPlayer() {
		return isPlayer(getLastJoinedRoom());
	}

	public boolean isSpectator() {
		return isSpectator(getLastJoinedRoom());
	}

	public boolean isPlayer(Room room) {
		return getPlayerId(room) > 0;
	}

	public boolean isSpectator(Room room) {
		return getPlayerId(room) < 0;
	}

	public Object getProperty(Object key) {
		return this.properties.get(key);
	}

	public void setProperty(Object key, Object val) {
		this.properties.put(key, val);
	}

	public boolean containsProperty(Object key) {
		return this.properties.containsKey(key);
	}

	public void removeProperty(Object key) {
		this.properties.remove(key);
	}

	public int getVariablesCount() {
		return this.variables.size();
	}

	public UserVariable getVariable(String varName) {
		return (UserVariable) this.variables.get(varName);
	}

	public void setVariable(UserVariable var) throws SFSVariableException {
		String varName = var.getName();

		if (var.getType() == VariableType.NULL) {
			removeVariable(varName);
		} else {
			if (!containsVariable(varName)) {
				if (this.variables.size() >= this.maxVariablesAllowed) {
					throw new SFSVariableException("The max number of variables (" + this.maxVariablesAllowed
							+ ") for this User: " + this.name + " was reached. Discarding variable: " + varName);
				}
			}

			this.variables.put(varName, var);

			if (this.logger.isDebugEnabled())
				this.logger.debug(String.format("UserVar set: %s, %s ", new Object[] { var, this }));
		}
	}

	public void setVariables(List<UserVariable> userVariables) throws SFSVariableException {
		for (UserVariable uVar : userVariables) {
			setVariable(uVar);
		}
	}

	public boolean containsVariable(String varName) {
		return this.variables.containsKey(varName);
	}

	public List<UserVariable> getVariables() {
		return new ArrayList<UserVariable>(this.variables.values());
	}

	public void removeVariable(String varName) {
		this.variables.remove(varName);

		if (this.logger.isDebugEnabled())
			this.logger.debug(String.format("UserVar removed: %s, %s", new Object[] { varName, this }));
	}

	public String toString() {
		return String.format("( User Name: %s, Id: %s, Priv: %s, Sess: %s ) ",
				new Object[] { this.name, Integer.valueOf(this.id), Short.valueOf(this.privilegeId), "" });
	}

	public int getBadWordsWarnings() {
		return this.badWordsWarnings;
	}

	public void setBadWordsWarnings(int badWordsWarnings) {
		this.badWordsWarnings = badWordsWarnings;
	}

	public int getFloodWarnings() {
		return this.floodWarnings;
	}

	public void setFloodWarnings(int floodWarnings) {
		this.floodWarnings = floodWarnings;
	}

	public long getLastLoginTime() {
		return this.lastLoginTime;
	}

	public boolean isBeingKicked() {
		return this.beingKicked;
	}

	public void setBeingKicked(boolean flag) {
		this.beingKicked = flag;
	}

	public IGameArray getUserVariablesData() {
		IGameArray variablesData = GameArray.newInstance();

		for (UserVariable var : this.variables.values()) {
			if (var.isHidden()) {
				continue;
			}
			variablesData.addGameArray(var.toSFSArray());
		}

		return variablesData;
	}

	public IGameArray toGameArray(Room room) {
		IGameArray userObj = GameArray.newInstance();

		userObj.addInt(this.id);
		userObj.addUtfString(this.name);
		userObj.addShort(this.privilegeId);
		userObj.addShort((short) getPlayerId(room));
		userObj.addGameArray(getUserVariablesData());

		return userObj;
	}

	public IGameArray toGameArray() {
		return toGameArray(getLastJoinedRoom());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof User)) {
			return false;
		}
		User user = (User) obj;
		boolean isEqual = false;

		if (user.getId() == this.id) {
			isEqual = true;
		}
		return isEqual;
	}

	public String getDump() {
		StringBuilder sb = new StringBuilder("/////////////// User Dump ////////////////").append("\n");

		sb.append("\tName: ").append(this.name).append("\n").append("\tId: ").append(this.id).append("\n")
				.append("\tHash: ").append("").append("\n").append("\tIP Address: ").append(ipAddress).append("\n")
				.append("\tPrivilegeId: ").append(getPrivilegeId()).append("\n").append("\tisSubscribed Groups: ")
				.append(getSubscribedGroups()).append("\n").append("\tLast Joined Room: ").append(getLastJoinedRoom())
				.append("\n").append("\tJoined Rooms: ").append(getJoinedRooms()).append("\n");

		if (this.variables.size() > 0) {
			sb.append("\tUserVariables: ").append("\n");

			for (UserVariable var : this.variables.values()) {
				sb.append("\t\t").append(var.toString()).append("\n");
			}
		}

		if (this.properties.size() > 0) {
			sb.append("\tProperties: ").append("\n");

			for (Iterator<Object> iterator = this.properties.keySet().iterator(); iterator.hasNext();) {
				Object key = iterator.next();

				sb.append("\t\t").append(key).append(": ").append(this.properties.get(key)).append("\n");
			}
		}

		sb.append("/////////////// End Dump /////////////////").append("\n");

		return sb.toString();
	}

	public List<User> getLastProxyList() {
		return this.proxyList;
	}

	public void setLastProxyList(List<User> proxyList) {
		this.proxyList = proxyList;
	}

	public List<BaseMMOItem> getLastMMOItemsList() {
		return this.mmoItemsList;
	}

	public void setLastMMOItemsList(List<BaseMMOItem> mmoItemsList) {
		this.mmoItemsList = mmoItemsList;
	}

	public MMORoom getLastJoinedMMORoom() {
		return lastJoinedMMORoom;
	}

	public void setLastJoinedMMORoom(MMORoom lastJoinedMMORoom) {
		this.lastJoinedMMORoom = lastJoinedMMORoom;
	}

	public MMORoom getCurrentMMORoom() {
		Room mmoRoom = null;

		for (Room r : getJoinedRooms()) {
			if (!(r instanceof MMORoom))
				continue;
			mmoRoom = r;
			break;
		}

		return (MMORoom) mmoRoom;
	}

	public Zone getZone() {
		return this.currentZone;
	}

	public void setZone(Zone currentZone) {
		if (this.currentZone != null) {
			throw new IllegalStateException("The User Zone is already set. It cannot be modified at Runtime. " + this);
		}
		this.currentZone = currentZone;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	
	
}