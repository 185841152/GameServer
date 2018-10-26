package com.net.server.entities.managers;

import com.net.business.vo.SeatingVo;
import com.net.engine.service.BaseCoreService;
import com.net.server.GameServer;
import com.net.server.api.CreateRoomSettings;
import com.net.server.entities.*;
import com.net.server.exceptions.*;
import com.net.server.mmo.CreateMMORoomSettings;
import com.net.server.mmo.MMORoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameRoomManager extends BaseCoreService implements IRoomManager {
	private final Map<Integer, Room> roomsById;
	private final Map<String, Room> roomsByName;
	private final Map<String, List<Room>> roomsByGroup;
	private final List<String> groups;
	private final AtomicInteger gameRoomCounter;
	private Logger logger;
	private GameServer server;
	private Zone ownerZone;

	public GameRoomManager() {
		this.server = GameServer.getInstance();
		this.logger = LoggerFactory.getLogger(getClass());

		this.roomsById = new ConcurrentHashMap<Integer, Room>();
		this.roomsByName = new ConcurrentHashMap<String, Room>();
		this.roomsByGroup = new ConcurrentHashMap<String, List<Room>>();
		this.groups = new ArrayList<String>();
		this.gameRoomCounter = new AtomicInteger();
	}

	public Room createRoom(CreateRoomSettings params) throws SFSCreateRoomException {
		return createRoom(params, null);
	}

	public Room createRoom(CreateRoomSettings params, User owner) throws SFSCreateRoomException {
		String roomName = params.getName();
		int roomId = this.gameRoomCounter.getAndIncrement()+100000;
		//生成ID
//		int roomId=Utils.randomByScope(1000000, 9999999);
//		while(ownerZone.getRoomById(roomId)!=null){
//			roomId=Utils.randomByScope(1000000, 9999999);
//		}
		roomName=roomName+roomId;
		try {
			validateRoomName(roomName);
		} catch (SFSRoomException roomExc) {
			throw new SFSCreateRoomException(roomExc.getMessage(), roomExc.getErrorData());
		}
		Room newRoom;
		if ((params instanceof CreateMMORoomSettings)) {
			newRoom = new MMORoom(roomName, ((CreateMMORoomSettings) params).getDefaultAOI(),
					((CreateMMORoomSettings) params).getProximityListUpdateMillis());
		} else {
			newRoom = new GameRoom(roomName);
		}
		newRoom.setZone(this.ownerZone);
		newRoom.setGroupId(params.getGroupId());
		newRoom.setPassword(params.getPassword());
		newRoom.setDynamic(params.isDynamic());
		newRoom.setHidden(params.isHidden());
		newRoom.setMaxUsers(params.getMaxUsers());
		newRoom.setId(roomId);
		
		if (params.isGame())
			newRoom.setMaxSpectators(params.getMaxSpectators());
		else {
			newRoom.setMaxSpectators(0);
		}

		newRoom.setMaxRoomVariablesAllowed(params.getMaxVariablesAllowed());

		if (params.getRoomVariables() != null) {
			newRoom.setVariables(params.getRoomVariables());
		}

		Set<GameRoomSettings> roomSettings = params.getRoomSettings();
		if (roomSettings == null) {
			if ((params instanceof CreateMMORoomSettings)) {
				roomSettings = EnumSet.of(GameRoomSettings.USER_COUNT_CHANGE_EVENT,
						GameRoomSettings.USER_VARIABLES_UPDATE_EVENT, GameRoomSettings.PUBLIC_MESSAGES);
			} else {
				roomSettings = EnumSet.of(GameRoomSettings.USER_ENTER_EVENT, GameRoomSettings.USER_EXIT_EVENT,
						GameRoomSettings.USER_COUNT_CHANGE_EVENT, GameRoomSettings.USER_VARIABLES_UPDATE_EVENT,
						GameRoomSettings.PUBLIC_MESSAGES);
			}

		}

		if ((newRoom instanceof MMORoom)) {
			if (roomSettings.contains(GameRoomSettings.USER_ENTER_EVENT)) {
				roomSettings.remove(GameRoomSettings.USER_ENTER_EVENT);
			}
			if (roomSettings.contains(GameRoomSettings.USER_EXIT_EVENT)) {
				roomSettings.remove(GameRoomSettings.USER_EXIT_EVENT);
			}
		}
		newRoom.setUseWordsFilter(params.isUseWordsFilter());
		newRoom.setFlags(roomSettings);
		newRoom.setOwner(owner);
		newRoom.setAutoRemoveMode(params.getAutoRemoveMode());

		if (params.getRoomProperties() != null) {
			((GameRoom) newRoom).setProperties(params.getRoomProperties());
		}

		addRoom(newRoom);

		newRoom.setActive(true);

		if (newRoom.isGame()) {
			this.gameRoomCounter.incrementAndGet();
		}
		this.logger.info(String.format("Room created: %s, %s",
				new Object[] { newRoom.getZone().toString(), newRoom.toString() }));

		//分配位置，按照加入的先後顺序分配
		SeatingVo seatingVo=new SeatingVo();
		newRoom.setProperty("seating", seatingVo);
		return newRoom;
	}

	public void addGroup(String groupId) {
		synchronized (this.groups) {
			this.groups.add(groupId);
		}
	}

	public void addRoom(Room room) {
		this.roomsById.put(Integer.valueOf(room.getId()), room);
		this.roomsByName.put(room.getName(), room);

		synchronized (this.groups) {
			if (!this.groups.contains(room.getGroupId())) {
				this.groups.add(room.getGroupId());
			}
		}
		addRoomToGroup(room);
	}

	public boolean containsGroup(String groupId) {
		boolean flag = false;

		synchronized (this.groups) {
			flag = this.groups.contains(groupId);
		}

		return flag;
	}

	public List<String> getGroups() {
		List<String> groupsCopy = null;

		synchronized (this.groups) {
			groupsCopy = new ArrayList<String>(this.groups);
		}

		return groupsCopy;
	}

	public Room getRoomById(int id) {
		return (Room) this.roomsById.get(Integer.valueOf(id));
	}

	public Room getRoomByName(String name) {
		return (Room) this.roomsByName.get(name);
	}

	public List<Room> getRoomList() {
		return new ArrayList<Room>(this.roomsById.values());
	}

	public List<Room> getRoomListFromGroup(String groupId) {
		List<Room> roomList = this.roomsByGroup.get(groupId);
		List<Room> copyOfRoomList = null;

		if (roomList != null) {
			synchronized (roomList) {
				copyOfRoomList = new ArrayList<Room>(roomList);
			}
		}

		copyOfRoomList = new ArrayList<Room>();

		return copyOfRoomList;
	}

	public int getGameRoomCount() {
		return this.gameRoomCounter.get();
	}

	public int getTotalRoomCount() {
		return this.roomsById.size();
	}

	public void removeGroup(String groupId) {
		synchronized (this.groups) {
			this.groups.remove(groupId);
		}
	}

	public void removeRoom(int roomId) {
		Room room = (Room) this.roomsById.get(Integer.valueOf(roomId));

		if (room == null)
			this.logger.warn("Can't remove requested room. ID = " + roomId + ". Room was not found.");
		else
			removeRoom(room);
	}

	public void removeRoom(String name) {
		Room room = (Room) this.roomsByName.get(name);

		if (room == null)
			this.logger.warn("Can't remove requested room. Name = " + name + ". Room was not found.");
		else
			removeRoom(room);
	}

	public void removeRoom(Room room) {
		try {
			room.destroy();
			room.setActive(false);

			boolean wasRemoved = this.roomsById.remove(Integer.valueOf(room.getId())) != null;
			this.roomsByName.remove(room.getName());
			removeRoomFromGroup(room);

			if ((wasRemoved) && (room.isGame())) {
				this.gameRoomCounter.decrementAndGet();
			}
			this.logger.info(String.format("Room removed: %s, %s, Duration: %s",
					new Object[] { room.getZone().toString(), room.toString(), Long.valueOf(room.getLifeTime()) }));
		} finally {

		}
	}

	public boolean containsRoom(int id, String groupId) {
		Room room = (Room) this.roomsById.get(Integer.valueOf(id));
		return isRoomContainedInGroup(room, groupId);
	}

	public boolean containsRoom(int id) {
		return this.roomsById.containsKey(Integer.valueOf(id));
	}

	public boolean containsRoom(Room room, String groupId) {
		return isRoomContainedInGroup(room, groupId);
	}

	public boolean containsRoom(Room room) {
		return this.roomsById.containsValue(room);
	}

	public boolean containsRoom(String name, String groupId) {
		Room room = (Room) this.roomsByName.get(name);
		return isRoomContainedInGroup(room, groupId);
	}

	public boolean containsRoom(String name) {
		return this.roomsByName.containsKey(name);
	}

	public Zone getOwnerZone() {
		return this.ownerZone;
	}

	public void setOwnerZone(Zone zone) {
		this.ownerZone = zone;
	}

	public void removeUser(User user) {
		for (Room room : user.getJoinedRooms()) {
			removeUser(user, room);
		}
	}

	public void removeUser(User user, Room room) {
		try {
			if (room.containsUser(user)) {
				room.removeUser(user);
				this.logger.debug("User: " + user.getName() + " removed from Room: " + room.getName());
			} else {
				throw new SFSRuntimeException("Can't remove user: " + user + ", from: " + room);
			}
		} finally {
			handleAutoRemove(room);
		}
	}

	public void checkAndRemove(Room room) {
		handleAutoRemove(room);
	}

	public void changeRoomName(Room room, String newName) throws SFSRoomException {
		if (room == null) {
			throw new IllegalArgumentException("Can't change name. Room is Null!");
		}
		if (!containsRoom(room)) {
			throw new IllegalArgumentException(room + " is not managed by this Zone: " + this.ownerZone);
		}

		validateRoomName(newName);

		String oldName = room.getName();

		room.setName(newName);

		this.roomsByName.put(newName, room);
		this.roomsByName.remove(oldName);
	}

	public void changeRoomPasswordState(Room room, String password) {
		if (room == null) {
			throw new IllegalArgumentException("Can't change password. Room is Null!");
		}
		if (!containsRoom(room)) {
			throw new IllegalArgumentException(room + " is not managed by this Zone: " + this.ownerZone);
		}
		room.setPassword(password);
	}

	public void changeRoomCapacity(Room room, int newMaxUsers, int newMaxSpect) {
		if (room == null) {
			throw new IllegalArgumentException("Can't change password. Room is Null!");
		}
		if (!containsRoom(room)) {
			throw new IllegalArgumentException(room + " is not managed by this Zone: " + this.ownerZone);
		}
		if (newMaxUsers > 0) {
			room.setMaxUsers(newMaxUsers);
		}
		if (newMaxSpect >= 0)
			room.setMaxSpectators(newMaxSpect);
	}

	private void handleAutoRemove(Room room) {
		if ((room.isEmpty()) && (room.isDynamic())) {
			switch (room.getAutoRemoveMode().ordinal()) {
			case 1:
				if (room.isGame())
					removeWhenEmpty(room);
				else {
					removeWhenEmptyAndCreatorIsGone(room);
				}
				break;
			case 2:
				removeWhenEmpty(room);
				break;
			case 3:
				removeWhenEmptyAndCreatorIsGone(room);
			case 4:
			}
		}
	}

	private void removeWhenEmpty(Room room) {
		if (room.isEmpty())
			this.server.getAPIManager().getSFSApi().removeRoom(room);
	}

	private void removeWhenEmptyAndCreatorIsGone(Room room) {
		User owner = room.getOwner();

		if (owner != null)
			this.server.getAPIManager().getSFSApi().removeRoom(room);
	}

	private boolean isRoomContainedInGroup(Room room, String groupId) {
		boolean flag = false;

		if ((room != null) && (room.getGroupId().equals(groupId)) && (containsGroup(groupId))) {
			flag = true;
		}
		return flag;
	}

	private void addRoomToGroup(Room room) {
		String groupId = room.getGroupId();

		List<Room> roomList = this.roomsByGroup.get(groupId);

		if (roomList == null) {
			roomList = new ArrayList<Room>();
			this.roomsByGroup.put(groupId, roomList);
		}

		synchronized (roomList) {
			roomList.add(room);
		}
	}

	private void removeRoomFromGroup(Room room) {
		List<Room> roomList = this.roomsByGroup.get(room.getGroupId());

		if (roomList != null) {
			synchronized (roomList) {
				roomList.remove(room);
			}
		}

		this.logger.info("Cannot remove room: " + room.getName() + " from it's group: " + room.getGroupId()
				+ ". The group was not found.");
	}

	private void validateRoomName(String roomName) throws SFSRoomException {
		if (containsRoom(roomName)) {
			ErrorData errorData = new ErrorData(ErrorCode.ROOM_DUPLICATE_NAME);
			errorData.addParameter(roomName);

			String message = String.format("A room with the same name already exists: %s", new Object[] { roomName });
			throw new SFSRoomException(message, errorData);
		}

		int nameLen = roomName.length();
		int minLen = 5;
		int maxLen = 20;

		if ((nameLen < minLen) || (nameLen > maxLen)) {
			ErrorData errorData = new ErrorData(ErrorCode.ROOM_NAME_BAD_SIZE);
			errorData.addParameter(String.valueOf(minLen));
			errorData.addParameter(String.valueOf(maxLen));
			errorData.addParameter(String.valueOf(nameLen));

			String message = String.format("Room name length is out of valid range. Min: %s, Max: %s, Found: %s (%s)",
					new Object[] { Integer.valueOf(minLen), Integer.valueOf(maxLen), Integer.valueOf(nameLen),
							roomName });
			throw new SFSRoomException(message, errorData);
		}

	}

	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

}