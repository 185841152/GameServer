package com.net.server.entities;

import com.net.business.beans.BeanManager;
import com.net.business.entity.AppUser;
import com.net.engine.sessions.ISession;
import com.net.server.GameServer;
import com.net.server.api.CreateRoomSettings;
import com.net.server.data.GameArray;
import com.net.server.data.IGameArray;
import com.net.server.entities.managers.GameRoomManager;
import com.net.server.entities.managers.GameUserManager;
import com.net.server.entities.managers.IRoomManager;
import com.net.server.entities.managers.IUserManager;
import com.net.server.exceptions.*;
import com.net.server.util.IResponseThrottler;
import com.net.server.util.UserCountChangeResponseThrottler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 游戏分区实现
 * 
 * @author sj 2016-07-26 15:50:44
 *
 */
public class GameZone implements Zone {
	private Logger logger;
	private final Integer id;
	private final String name;
	private GameServer server;
	private List<String> defaultGroups;
	private ConcurrentMap<Object, Object> properties;
	private volatile int maxAllowedUsers;
	private volatile int userReconnectionSeconds = 0;
	private int maxUserIdleTime = 0;
	private final IRoomManager roomManager;
	private final IUserManager userManager;

	private IResponseThrottler uCountResponseThrottler;
	private volatile int userCountChangeUpdateInterval = 0;

	public GameZone(String name, Integer id) {
		this.id = id;
		this.name = name;
		this.logger = LoggerFactory.getLogger(getClass());
		this.server = GameServer.getInstance();

		this.roomManager = new GameRoomManager();
		this.roomManager.setOwnerZone(this);

		this.userManager = new GameUserManager();

		this.properties = new ConcurrentHashMap<Object, Object>();

		this.roomManager.addGroup("default");

	}

	public User login(ISession session, String userName,Integer userId) throws SFSLoginException {
		try {
			// 创建游戏用户
			User user=null;
			User oldUser=server.getUserManager().getUserById(userId);
			if (oldUser!=null) {
				user = oldUser;
				//查询用户信息
				Query query=Query.query(Criteria.where("id").is(userId));
				AppUser appUser=BeanManager.getInstance().getMongoTemplate().findOne(query,AppUser.class,AppUser.ENTITY_NAME);
				user.setProperty("userInfo",appUser);
			}else {
				user=new GameUser(userId, userName);
				user.setZone(this);
				//查询用户信息
				Query query=Query.query(Criteria.where("id").is(userId));
				AppUser appUser= BeanManager.getInstance().getMongoTemplate().findOne(query,AppUser.class,AppUser.ENTITY_NAME);
				if (appUser==null){
					ErrorData errorData = new ErrorData(ErrorCode.LOGIN_BAD_USERNAME);
					errorData.addParameter(userName);
					throw new SFSLoginException("登录失败,用户信息不存在", errorData);
				}
				user.setProperty("userInfo",appUser);
				// 保存用户到用户管理类
				this.userManager.addUser(user);
				this.server.getUserManager().addUser(user);
			}
			//创建网关用户
			user.setConnected(true);
			user.setLastLoginTime(System.currentTimeMillis());
			user.setSessionId(session.getId());
			logger.info("用户{0}登录成功", userName);
			return user;
		} catch (Exception e) {
			ErrorData errorData = new ErrorData(ErrorCode.LOGIN_BAD_USERNAME);
			errorData.addParameter(userName);

			throw new SFSLoginException(e.getMessage(), errorData);
		}

	}

	public Object getProperty(Object key) {
		return this.properties.get(key);
	}

	public void removeProperty(Object key) {
		this.properties.remove(key);
	}

	public boolean containsProperty(Object key) {
		return this.properties.containsKey(key);
	}

	public void setProperty(Object key, Object value) {
		this.properties.put(key, value);
	}

	public int getMaxMembers() {
		return this.maxAllowedUsers;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return this.name;
	}

	public IUserManager getUserManager() {
		return this.userManager;
	}

	public IRoomManager getRoomManager() {
		return this.roomManager;
	}

	public void setMaxAllowedUsers(int maxAllowedUsers) {
		this.maxAllowedUsers = maxAllowedUsers;
	}

	public int getUserReconnectionSeconds() {
		return this.userReconnectionSeconds;
	}

	public void setUserReconnectionSeconds(int seconds) {
		this.userReconnectionSeconds = seconds;
	}

	public int getMaxUserIdleTime() {
		return this.maxUserIdleTime;
	}

	public void setMaxUserIdleTime(int seconds) {
		this.maxUserIdleTime = seconds;
	}

	public void removeUser(int userId) {
		User user = this.userManager.getUserById(userId);

		if (user == null)
			this.logger.info("Can't remove user with Id: " + userId + ". User doesn't exist in Zone: " + this.name);
		else
			removeUser(user);
	}

	public void removeUser(String userName) {
		User user = this.userManager.getUserByName(userName);

		if (user == null)
			this.logger.info("Can't remove user with Name: " + userName + ". User doesn't exist in Zone: " + this.name);
		else
			removeUser(user);
	}

	public void removeUser(User user) {
		this.userManager.disconnectUser(user);

		this.roomManager.removeUser(user);
	}

	public void checkAndRemove(Room room) {
		this.roomManager.checkAndRemove(room);
	}

	public void setUserCountChangeUpdateInterval(int interval) {
		if (interval < 0) {
			throw new GameRuntimeException(
					"Negative values are not acceptable for Zone.userCountChangeUpdateInterval: " + interval);
		}

		synchronized (this) {
			this.userCountChangeUpdateInterval = interval;
		}

		if (this.uCountResponseThrottler == null) {
			this.uCountResponseThrottler = new UserCountChangeResponseThrottler(this.userCountChangeUpdateInterval,
					this.name);
		} else {
			this.uCountResponseThrottler.setInterval(this.userCountChangeUpdateInterval);
		}

	}

	public Room createRoom(CreateRoomSettings params, User user) throws SFSCreateRoomException {
		return this.roomManager.createRoom(params, user);
	}

	public Room createRoom(CreateRoomSettings params) throws SFSCreateRoomException {
		return this.roomManager.createRoom(params);
	}

	public IResponseThrottler getUCountThrottler() {
		return this.uCountResponseThrottler;
	}

	public int getUserCountChangeUpdateInterval() {
		return this.userCountChangeUpdateInterval;
	}

	@Override
	public List<Room> getRoomList() {
		return this.roomManager.getRoomList();
	}

	@Override
	public List<Room> getRoomListFromGroup(String groupId) {
		return this.roomManager.getRoomListFromGroup(groupId);
	}

	@Override
	public Room getRoomById(int roomId) {
		return this.roomManager.getRoomById(roomId);
	}

	@Override
	public Room getRoomByName(String roomName) {
		return this.roomManager.getRoomByName(roomName);
	}

	@Override
	public void addRoom(Room room) {
		this.roomManager.addRoom(room);
	}

	@Override
	public void removeRoom(Room room) {
		this.roomManager.removeRoom(room);

	}

	@Override
	public void removeRoom(int roomId) {
		this.roomManager.removeRoom(roomId);

	}

	@Override
	public void removeRoom(String roomName) {
		this.roomManager.removeRoom(roomName);

	}

	@Override
	public Collection<User> getUsersInGroup(String groupId) {
		Set<User> userList = new HashSet<User>();
		for (Room room : this.roomManager.getRoomListFromGroup(groupId)) {
			userList.addAll(room.getUserList());
		}
		return userList;
	}

	@Override
	public Collection<User> getUserList() {
		return this.userManager.getAllUsers();
	}

	@Override
	public void removeAllUsers() {
		this.userManager.getAllUsers().clear();
	}

	@Override
	public IGameArray getRoomListData() {
		return getRoomListData(this.defaultGroups);
	}

	@Override
	public IGameArray getRoomListData(List<String> groupIds) {
		IGameArray roomList = GameArray.newInstance();
		if (groupIds.size() > 0) {
			for (String groupId : groupIds) {
				List<Room> roomsInGroup = getRoomListFromGroup(groupId);

				if (roomsInGroup == null)
					continue;
				for (Room room : roomsInGroup) {
					roomList.addGameArray(room.toSFSArray(true));
				}
			}
		}

		return roomList;
	}
	

	public void removeUserFromRoom(User user, Room room){
		this.roomManager.removeUser(user, room);
	}

	@Override
	public List<String> getDefaultGroups() {
		return new ArrayList<String>(this.defaultGroups);
	}

	@Override
	public void setDefaultGroups(List<String> groupIDs) {
		this.defaultGroups = groupIDs;
	}
}
