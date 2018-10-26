package com.net.server.entities;

import com.net.business.entity.AppUser;
import com.net.server.data.GameArray;
import com.net.server.data.GameObject;
import com.net.server.data.IGameArray;
import com.net.server.data.IGameObject;
import com.net.server.entities.managers.GameUserManager;
import com.net.server.entities.managers.IUserManager;
import com.net.server.entities.variables.RoomVariable;
import com.net.server.entities.variables.VariableType;
import com.net.server.exceptions.ErrorCode;
import com.net.server.exceptions.ErrorData;
import com.net.server.exceptions.SFSJoinRoomException;
import com.net.server.exceptions.SFSVariableException;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class GameRoom implements Room {
	private static AtomicInteger autoID = new AtomicInteger(0);
	private Zone zone;
	private int id;
	private String groupId;
	private String name;
	private String password;
	private boolean passwordProtected;
	private int maxUsers;
	private int maxSpectators;
	private int maxRoomVariablesAllowed;
	private User owner;
	private IUserManager userManager;
	private boolean dynamic;
	private boolean game;
	private boolean hidden;
	private volatile boolean active;
	private volatile boolean start;
	private GameRoomRemoveMode autoRemoveMode;
	private final long lifeTime;
	private final Map<Object, Object> properties;
	private final Map<String, RoomVariable> variables;
	
	
	private ScheduledFuture<?> schedule;

	private Set<GameRoomSettings> flags;
	private volatile boolean userWordsFilter;
	protected Logger logger;

	private static int getNewID() {
		return autoID.getAndIncrement();
	}

	public GameRoom(String name) {
		this(name, null);
	}

	public GameRoom(String name, Class<?> customPlayerIdGeneratorClass) {
		this.id = getNewID();
		this.name = name;
		this.active = false;

		this.logger = LoggerFactory.getLogger(getClass());
		this.properties = new ConcurrentHashMap<Object, Object>();
		this.variables = new ConcurrentHashMap<String, RoomVariable>();
		this.userManager = new GameUserManager();
		this.lifeTime = System.currentTimeMillis();
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id){
		this.id=id;
	}

	public String getGroupId() {
		if ((this.groupId != null) && (this.groupId.length() > 0)) {
			return this.groupId;
		}

		return "default";
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;

		if ((this.password != null) && (this.password.length() > 0))
			this.passwordProtected = true;
		else
			this.passwordProtected = false;
	}

	public boolean isPasswordProtected() {
		return this.passwordProtected;
	}

	public boolean isPublic() {
		return !this.passwordProtected;
	}

	public int getMaxUsers() {
		return this.maxUsers;
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;

	}

	public int getMaxSpectators() {
		return this.maxSpectators;
	}

	public void setMaxSpectators(int maxSpectators) {
		this.maxSpectators = maxSpectators;
	}

	public User getOwner() {
		return this.owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public IUserManager getUserManager() {
		return this.userManager;
	}

	public void setUserManager(IUserManager userManager) {
		this.userManager = userManager;
	}

	public boolean isDynamic() {
		return this.dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	public boolean isGame() {
		return this.game;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean flag) {
		this.active = flag;
	}

	public boolean isStart() {
		return this.start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public List<User> getPlayersList() {
		List<User> playerList = new ArrayList<User>();

		for (User user : this.userManager.getAllUsers()) {
			if (user.isPlayer(this)) {
				playerList.add(user);
			}
		}
		return playerList;
	}

	public Object getProperty(Object key) {
		return this.properties.get(key);
	}

	public RoomSize getSize() {
		int uCount = 0;
		int sCount = 0;

		if (this.game) {
			for (User user : this.userManager.getAllUsers()) {
				if (user.isSpectator(this))
					sCount++;
				else {
					uCount++;
				}
			}
		} else {
			uCount = this.userManager.getUserCount();
		}

		return new RoomSize(uCount, sCount);
	}

	public void removeProperty(Object key) {
		this.properties.remove(key);
	}

	public List<User> getSpectatorsList() {
		List<User> specList = new ArrayList<User>();

		for (User user : this.userManager.getAllUsers()) {
			if (user.isSpectator(this)) {
				specList.add(user);
			}
		}
		return specList;
	}

	public User getUserById(int id) {
		return this.userManager.getUserById(id);
	}

	public User getUserByName(String name) {
		return this.userManager.getUserByName(name);
	}


	public User getUserByPlayerId(int playerId) {
		User user = null;

		for (User u : this.userManager.getAllUsers()) {
			if (u.getPlayerId(this) != playerId)
				continue;
			user = u;
			break;
		}

		return user;
	}

	public List<User> getUserList() {
		return this.userManager.getAllUsers();
	}

	public int getVariablesCount() {
		return this.variables.size();
	}

	public RoomVariable getVariable(String varName) {
		return (RoomVariable) this.variables.get(varName);
	}

	public List<RoomVariable> getVariables() {
		return new ArrayList<RoomVariable>(this.variables.values());
	}

	public List<RoomVariable> getVariablesCreatedByUser(User user) {
		List<RoomVariable> varList = new ArrayList<RoomVariable>();

		for (RoomVariable rVar : this.variables.values()) {
			if (rVar.getOwner() == user) {
				varList.add(rVar);
			}
		}
		return varList;
	}

	public boolean containsProperty(Object key) {
		return this.properties.containsKey(key);
	}

	public void removeVariable(String varName) {
		this.variables.remove(varName);

		if (this.logger.isDebugEnabled())
			this.logger.debug("RoomVar deleted: " + varName + " in " + this);
	}

	public List<RoomVariable> removeVariablesCreatedByUser(User user) {
		List<RoomVariable> varList = getVariablesCreatedByUser(user);

		for (RoomVariable rVar : varList) {
			removeVariable(rVar.getName());

			rVar.setNull();
		}

		return varList;
	}

	public int getCapacity() {
		return this.maxUsers + this.maxSpectators;
	}

	public void setCapacity(int maxUser, int maxSpectators) {
		this.maxUsers = maxUser;
		this.maxSpectators = maxSpectators;
	}

	public void setMaxRoomVariablesAllowed(int max) {
		this.maxRoomVariablesAllowed = max;
	}

	public int getMaxRoomVariablesAllowed() {
		return this.maxRoomVariablesAllowed;
	}

	public void setFlags(Set<GameRoomSettings> settings) {
		this.flags = settings;
	}

	public boolean isFlagSet(GameRoomSettings flag) {
		return this.flags.contains(flag);
	}

	public void setFlag(GameRoomSettings flag, boolean state) {
		if (state) {
			this.flags.add(flag);
		} else {
			this.flags.remove(flag);
		}
	}

	public boolean isUseWordsFilter() {
		return this.userWordsFilter;
	}

	public void setUseWordsFilter(boolean useWordsFilter) {
		this.userWordsFilter = useWordsFilter;
	}

	public void setProperty(Object key, Object value) {
		this.properties.put(key, value);
	}

	public void setVariables(List<RoomVariable> variables) {
		setVariables(variables, false);
	}

	public void setVariables(List<RoomVariable> variables, boolean overrideOwnership) {
		for (RoomVariable var : variables) {
			try {
				setVariable(var);
			} catch (SFSVariableException e) {
				this.logger.warn(e.getMessage());
			}
		}
	}

	public void setVariable(RoomVariable roomVariable) throws SFSVariableException {
		setVariable(roomVariable, false);
	}

	public void destroy() {

	}

	public void setVariable(RoomVariable roomVariable, boolean overrideOwnership) throws SFSVariableException {
		if (this.maxRoomVariablesAllowed < 1) {
			throw new SFSVariableException("Room Variables are disabled: " + toString());
		}
		String varName = roomVariable.getName();
		RoomVariable oldVariable = (RoomVariable) this.variables.get(varName);

		if (roomVariable.getType() == VariableType.NULL) {
			if (oldVariable == null) {
				throw new SFSVariableException("Cannot delete non-existent Room Variable called: "
						+ roomVariable.getName() + ", Owner: " + roomVariable.getOwner());
			}
			deleteVariable(oldVariable, roomVariable, overrideOwnership);
		} else if (oldVariable != null) {
			modifyVariable(oldVariable, roomVariable, overrideOwnership);
		} else {
			addVariable(roomVariable, overrideOwnership);
		}
	}

	private void addVariable(RoomVariable var, boolean overrideOwnership) throws SFSVariableException {
		if (this.variables.size() >= this.maxRoomVariablesAllowed) {
			throw new SFSVariableException(String.format(
					"The max number of variables (%s) for this Room: %s was reached. Discarding variable: %s",
					new Object[] { Integer.valueOf(this.maxRoomVariablesAllowed), this.name, var.getName() }));
		}

		this.variables.put(var.getName(), var);

		if (this.logger.isDebugEnabled())
			this.logger.debug(String.format("RoomVar created: %s in %s ", new Object[] { var, this }));
	}

	private void modifyVariable(RoomVariable oldVariable, RoomVariable newVariable, boolean overrideOwnership)
			throws SFSVariableException {
		if (overrideOwnership) {
			overwriteVariable(oldVariable, newVariable);
		} else if (oldVariable.isPrivate()) {
			if (oldVariable.getOwner() == newVariable.getOwner())
				overwriteVariable(oldVariable, newVariable);
			else
				throw new SFSVariableException(String.format("Variable: %s cannot be changed by user: %s",
						new Object[] { oldVariable, newVariable.getOwner() }));
		} else
			overwriteVariable(oldVariable, newVariable);
	}

	private void overwriteVariable(RoomVariable oldRv, RoomVariable newRv) {
		if (oldRv.getOwner() == null) {
			newRv.setOwner(null);
		}

		newRv.setHidden(oldRv.isHidden());
		newRv.setGlobal(oldRv.isGlobal());

		this.variables.put(newRv.getName(), newRv);

		if (this.logger.isDebugEnabled())
			this.logger.debug(String.format("RoomVar changed: %s in %s ", new Object[] { newRv, this }));
	}

	private void deleteVariable(RoomVariable oldVariable, RoomVariable newVariable, boolean overrideOwnership)
			throws SFSVariableException {
		if (overrideOwnership) {
			removeVariable(oldVariable.getName());
		} else if (oldVariable.isPrivate()) {
			if (oldVariable.getOwner() == newVariable.getOwner())
				removeVariable(oldVariable.getName());
			else
				throw new SFSVariableException(
						"Variable: " + oldVariable + " cannot be deleted by user: " + newVariable.getOwner());
		} else
			removeVariable(oldVariable.getName());
	}

	public boolean containsVariable(String varName) {
		return this.variables.containsKey(varName);
	}

	public boolean containsUser(String name) {
		return this.userManager.containsName(name);
	}

	public boolean containsUser(User user) {
		return this.userManager.containsUser(user);
	}

	public void addUser(User user) throws SFSJoinRoomException {
		addUser(user, false);
	}

	public void addUser(User user, boolean asSpectator) throws SFSJoinRoomException {
		if (this.userManager.containsId(user.getId())) {
			String message = String.format("User already joined: %s, Room: %s", new Object[] { user, this });
			ErrorData data = new ErrorData(ErrorCode.JOIN_ALREADY_JOINED);
			data.addParameter(this.name);

			throw new SFSJoinRoomException(message, data);
		}

		boolean okToAdd = false;

		synchronized (this) {
			RoomSize roomSize = getSize();

			if ((isGame()) && (asSpectator)) {
				okToAdd = roomSize.getSpectatorCount() < this.maxSpectators;
			} else {
				okToAdd = roomSize.getUserCount() < this.maxUsers;
			}

			if (!okToAdd) {
				String message = String.format("Room is full: %s - Can't add User: %s ",
						new Object[] { this.name, user });
				ErrorData data = new ErrorData(ErrorCode.JOIN_ROOM_FULL);
				data.addParameter(this.name);

				throw new SFSJoinRoomException(message, data);
			}

			this.userManager.addUser(user);
		}

		user.addJoinedRoom(this);

		user.setPlayerId(user.getId(), this);
	}

	public void removeUser(User user) {
		this.userManager.removeUser(user);
		user.removeJoinedRoom(this);
	}

	public long getLifeTime() {
		return System.currentTimeMillis() - this.lifeTime;
	}

	public boolean isEmpty() {
		return this.userManager.getUserCount() == 0;
	}

	public boolean isFull() {
		if (isGame()) {
			return getSize().getUserCount() == this.maxUsers;
		}
		return this.userManager.getUserCount() == this.maxUsers;
	}

	public IGameArray getUserListData() {
		IGameArray userListData = GameArray.newInstance();
		PropertyFilter filter = new PropertyFilter(){
			public boolean apply(Object object, String fieldName,
								 Object fieldValue) {
				return null == fieldValue;
			}
		};
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setJsonPropertyFilter(filter);
		for (User user : this.userManager.getAllUsers()) {
			AppUser appUser= (AppUser) user.getProperty("userInfo");
			JSONObject obj=JSONObject.fromObject(appUser,jsonConfig);
			IGameObject userObj= GameObject.newFromJsonData(obj.toString());
			userObj.putBool("ol", user.isConnected());
			userObj.putInt("idx",(int)user.getProperty("idx"));
			userListData.addGameObject(userObj);
		}

		return userListData;
	}

	public IGameArray getRoomVariablesData(boolean globalsOnly) {
		IGameArray variablesData = GameArray.newInstance();

		for (RoomVariable var : this.variables.values()) {
			if (var.isHidden()) {
				continue;
			}
			if ((globalsOnly) && (!var.isGlobal())) {
				continue;
			}
			variablesData.addGameArray(var.toSFSArray());
		}

		return variablesData;
	}

	public String toString() {
		return String.format("[ Room: %s, Id: %s, Group: %s, isGame: %s ]",
				new Object[] { this.name, Integer.valueOf(this.id), this.groupId, Boolean.valueOf(this.game) });
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Room)) {
			return false;
		}
		Room room = (Room) obj;
		boolean isEqual = false;

		if (room.getId() == this.id) {
			isEqual = true;
		}
		return isEqual;
	}

	public IGameArray toSFSArray(boolean globalRoomVarsOnly) {
		RoomSize roomSize = getSize();

		IGameArray roomObj = GameArray.newInstance();
		roomObj.addInt(this.id);
		roomObj.addUtfString(this.name);
		roomObj.addUtfString(this.groupId);

		roomObj.addBool(isGame());
		roomObj.addBool(isHidden());
		roomObj.addBool(isPasswordProtected());

		roomObj.addShort((short) roomSize.getUserCount());
		roomObj.addShort((short) this.maxUsers);

		roomObj.addGameArray(getRoomVariablesData(globalRoomVarsOnly));

		if (isGame()) {
			roomObj.addShort((short) roomSize.getSpectatorCount());
			roomObj.addShort((short) this.maxSpectators);
		}

		return roomObj;
	}

	public GameRoomRemoveMode getAutoRemoveMode() {
		return this.autoRemoveMode;
	}

	public void setAutoRemoveMode(GameRoomRemoveMode autoRemoveMode) {
		this.autoRemoveMode = autoRemoveMode;
	}

	public ScheduledFuture<?> getSchedule() {
		return schedule;
	}

	public void setSchedule(ScheduledFuture<?> schedule) {
		this.schedule = schedule;
	}

	public void setProperties(Map<Object, Object> props) {
		this.properties.clear();
		this.properties.putAll(props);
	}

	public Zone getZone() {
		return this.zone;
	}

	public void setZone(Zone paramZone) {
		this.zone = paramZone;
	}

}