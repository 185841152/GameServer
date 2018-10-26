package com.net.server.entities.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.service.BaseCoreService;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.exceptions.SFSRuntimeException;

public final class GameUserManager extends BaseCoreService implements IUserManager {
	private final ConcurrentMap<String, User> usersByName;
	private final ConcurrentMap<Integer, User> usersById;
	private Room ownerRoom;
	private Logger logger;
	private int highestCCU = 0;

	public GameUserManager() {
		this.logger = LoggerFactory.getLogger(getClass());
		this.usersByName = new ConcurrentHashMap<String, User>();
		this.usersById = new ConcurrentHashMap<Integer, User>();
	}

	public void addUser(User user) {
		if (containsId(user.getId())) {
			throw new SFSRuntimeException(
					"Can't add User: " + user.getName() + " - Already exists in Room: " + this.ownerRoom);
		}
		this.usersById.put(Integer.valueOf(user.getId()), user);
		this.usersByName.put(user.getName(), user);

		if (this.usersById.size() > this.highestCCU)
			this.highestCCU = this.usersById.size();
	}

	public User getUserById(int id) {
		return (User) this.usersById.get(Integer.valueOf(id));
	}

	public User getUserByName(String name) {
		return (User) this.usersByName.get(name);
	}

	public void removeUser(int userId) {
		User user = (User) this.usersById.get(Integer.valueOf(userId));

		if (user == null)
			this.logger.warn("Can't remove user with ID: " + userId + ". User was not found.");
		else
			removeUser(user);
	}

	public void removeUser(String name) {
		User user = (User) this.usersByName.get(name);

		if (user == null)
			this.logger.warn("Can't remove user with name: " + name + ". User was not found.");
		else
			removeUser(user);
	}


	public void removeUser(User user) {
		this.usersById.remove(Integer.valueOf(user.getId()));
		this.usersByName.remove(user.getName());
	}

	public boolean containsId(int userId) {
		return this.usersById.containsKey(Integer.valueOf(userId));
	}

	public boolean containsName(String name) {
		return this.usersByName.containsKey(name);
	}

	public boolean containsUser(User user) {
		return this.usersById.containsValue(user);
	}

	public Room getOwnerRoom() {
		return this.ownerRoom;
	}

	public void setOwnerRoom(Room ownerRoom) {
		this.ownerRoom = ownerRoom;
	}

	public List<User> getAllUsers() {
		return new ArrayList<User>(this.usersById.values());
	}

	public Collection<User> getDirectUserList() {
		return Collections.unmodifiableCollection(this.usersById.values());
	}

	public int getUserCount() {
		return this.usersById.values().size();
	}

	public void disconnectUser(int userId) {
		User user = (User) this.usersById.get(Integer.valueOf(userId));

		if (user == null)
			this.logger.warn("Can't disconnect user with id: " + userId + ". User was not found.");
		else
			disconnectUser(user);
	}

	public void disconnectUser(String name) {
		User user = (User) this.usersByName.get(name);

		if (user == null)
			this.logger.warn("Can't disconnect user with name: " + name + ". User was not found.");
		else
			disconnectUser(user);
	}

	public void disconnectUser(User user) {
		removeUser(user);
	}

	public int getHighestCCU() {
		return this.highestCCU;
	}

	public String getOwnerDetails() {
		StringBuilder sb = new StringBuilder();

		if (this.ownerRoom != null) {
			sb.append("Room: ").append(this.ownerRoom.getName()).append(", Room Id: ").append(this.ownerRoom.getId());
		}

		return sb.toString();
	}

	public boolean isActive() {
		return false;
	}
}