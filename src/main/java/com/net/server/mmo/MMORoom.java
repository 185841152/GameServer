package com.net.server.mmo;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.net.server.GameServer;
import com.net.server.entities.GameRoom;
import com.net.server.entities.User;
import com.net.server.exceptions.SFSJoinRoomException;

public class MMORoom extends GameRoom {
	private static final int LIMBO_CLEANER_INTERVAL = 60;
	private final IProximityManager proximityManager;
	private final IMMOItemManager itemsManager;
	private final IMMOUpdateManager updateManager;
	private final ConcurrentMap<Integer, BaseMMOItem> itemsById;
	private Vec3D lowLimit;
	private Vec3D highLimit;
	private int userLimboMaxSeconds = 0;
	private ScheduledFuture<?> limboCleanerTask;
	private boolean sendAOIEntryPoint = false;

	public MMORoom(String name, Vec3D aoi, int updateMillis) {
		super(name);
		this.proximityManager = new ProximityManager(aoi);
		this.itemsManager = new MMOItemManager(aoi);
		this.updateManager = new MMOUpdateManager(this, updateMillis);
		this.itemsById = new ConcurrentHashMap<Integer, BaseMMOItem>();
	}

	public void addUser(User user, boolean asSpectator) throws SFSJoinRoomException {
		super.addUser(user, false);

		user.setLastProxyList(null);
	}

	public void removeUser(User user) {
		super.removeUser(user);

		try {
			this.proximityManager.removeUser(user);

			if (user.getLastProxyList() != null) {
				this.updateManager.addBatchToUpdate(user.getLastProxyList());
			} else {
				PreviousMMORoomState prevState = (PreviousMMORoomState) user.getProperty("PreviousMMORoomState");

				if (prevState != null) {
					if (prevState.getRoomId() != getId())
						this.updateManager.addBatchToUpdate(prevState.getProxyList());
				}
			}
		} catch (IllegalStateException err) {
			throw new IllegalStateException(
					"Remove failed. Requested user " + user + " was not found in this room: " + this);
		} finally {
			user.removeProperty("_uLoc");
		}
	}

	public void removeMMOItem(BaseMMOItem item) {
		this.itemsManager.removeItem(item);
		this.updateManager.addItemToUpdate(item);

		this.itemsById.remove(Integer.valueOf(item.getId()));
	}

	public Vec3D getDefaultAOI() {
		return this.proximityManager.getDefaultAOI();
	}

	public List<User> getProximityList(User target) {
		return this.proximityManager.getProximityList(target);
	}

	public List<User> getProximityList(User target, Vec3D aoi) {
		return this.proximityManager.getProximityList(target, aoi);
	}

	public List<User> getProximityList(Vec3D position) {
		return this.proximityManager.getProximityList(position);
	}

	public List<User> getProximityList(Vec3D position, Vec3D aoi) {
		return this.proximityManager.getProximityList(position, aoi);
	}

	public BaseMMOItem getMMOItemById(int itemId) {
		return (BaseMMOItem) this.itemsById.get(Integer.valueOf(itemId));
	}

	public List<BaseMMOItem> getAllMMOItems() {
		return new LinkedList<BaseMMOItem>(this.itemsById.values());
	}

	public boolean containsMMOItem(int id) {
		return this.itemsById.containsKey(Integer.valueOf(id));
	}

	public boolean containsMMOItem(BaseMMOItem item) {
		return this.itemsById.containsValue(item);
	}

	public List<BaseMMOItem> getProximityItems(User target) {
		return this.itemsManager.getItemList(target);
	}

	public List<BaseMMOItem> getProximityItems(User target, Vec3D aoi) {
		return this.itemsManager.getItemList(target, aoi);
	}

	public List<BaseMMOItem> getProximityItems(Vec3D pos) {
		return this.itemsManager.getItemList(pos);
	}

	public List<BaseMMOItem> getProximityItems(Vec3D pos, Vec3D aoi) {
		return this.itemsManager.getItemList(pos, aoi);
	}

	public Vec3D getSectorSize() {
		return this.proximityManager.getSectorSize();
	}

	public IProximityManager getProximityManager() {
		return this.proximityManager;
	}

	public IMMOItemManager getItemsManager() {
		return this.itemsManager;
	}

	public P3D findUserLocation(User user) {
		return ((ProximityManager) this.proximityManager).findUserLocation(user);
	}

	public P3D findItemLocation(BaseMMOItem item) {
		return ((MMOItemManager) this.itemsManager).findItemLocation(item);
	}

	public void updateUser(User user) {
		if (!containsUser(user)) {
			throw new IllegalArgumentException("Invalid User, not joined in this MMORoom: " + toString());
		}
		this.proximityManager.updateUser(user);
		this.updateManager.addUserToUpdate(user);
	}

	public void updateItem(BaseMMOItem item, Vec3D pos) {
		if (item.getRoom() == null) {
			item.setRoom(this);
		}

		if (item.getRoom() != this) {
			throw new IllegalArgumentException(
					String.format("Item: %s is already assigned to %s, and can't be re-assigned to %s",
							new Object[] { item.getRoom(), this }));
		}

		this.itemsManager.setItem(item, pos);
		this.updateManager.addItemToUpdate(item);

		this.itemsById.putIfAbsent(Integer.valueOf(item.getId()), item);
	}

	public Vec3D getMapLowerLimit() {
		return this.lowLimit;
	}

	public Vec3D getMapHigherLimit() {
		return this.highLimit;
	}

	public void setMapLimits(Vec3D lowLimit, Vec3D highLimit) {
		if ((this.lowLimit != null) || (this.highLimit != null)) {
			throw new IllegalStateException("Map Limits cannot be reset");
		}
		this.lowLimit = lowLimit;
		this.highLimit = highLimit;
	}

	public int getUserLimboMaxSeconds() {
		return this.userLimboMaxSeconds;
	}

	public void setUserLimboMaxSeconds(int userLimboMaxSeconds) {
		if (this.userLimboMaxSeconds > 0) {
			throw new IllegalStateException("UserLimboMaxSeconds cannot be reset");
		}

		this.userLimboMaxSeconds = userLimboMaxSeconds;
		this.limboCleanerTask = GameServer.getInstance().getTaskScheduler()
				.scheduleAtFixedRate(new MMORoomCleaner(this), 0, LIMBO_CLEANER_INTERVAL, TimeUnit.SECONDS);
	}

	public void destroy() {
		if (this.limboCleanerTask != null) {
			this.limboCleanerTask.cancel(true);
		}
		this.updateManager.destroy();
	}

	public boolean isSendAOIEntryPoint() {
		return this.sendAOIEntryPoint;
	}

	public void setSendAOIEntryPoint(boolean sendAOIEntryPoint) {
		this.sendAOIEntryPoint = sendAOIEntryPoint;
	}

	public int getProximityListUpdateMillis() {
		return this.updateManager.getUpdateThreshold();
	}

	public User getUserByPlayerId(int playerId) {
		return null;
	}

	public boolean isGame() {
		return false;
	}

	public List<User> getPlayersList() {
		throw new UnsupportedOperationException("MMO Room don't support players");
	}

	public List<User> getSpectatorsList() {
		throw new UnsupportedOperationException("MMO Rooms don't support spectators");
	}

	public static class PreviousMMORoomState {
		int roomId;
		List<User> proxyList;

		public PreviousMMORoomState(int roomId, List<User> proxyList) {
			this.roomId = roomId;
			this.proxyList = proxyList;
		}

		public List<User> getProxyList() {
			return this.proxyList;
		}

		public int getRoomId() {
			return this.roomId;
		}
	}
}