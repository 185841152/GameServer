package com.net.server.mmo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.net.server.api.response.IGameMMOResponseApi;
import com.net.server.api.response.GameMMOResponseApi;
import com.net.server.entities.User;

public final class MMOUpdateManager implements IMMOUpdateManager {
	private final MMORoom mmoRoom;
	private final List<User> usersToUpdate;
	private final List<BaseMMOItem> itemsToUpdate;
	private final ScheduledFuture<?> updateTask;
	private final IGameMMOResponseApi responseAPI;
	private volatile int threshold;

	public MMOUpdateManager(MMORoom room, int thresholdMillis) {
		this.mmoRoom = room;
		this.threshold = thresholdMillis;
		this.usersToUpdate = new LinkedList<User>();
		this.itemsToUpdate = new LinkedList<BaseMMOItem>();
		this.responseAPI = new GameMMOResponseApi();
		// SmartFoxServer.getInstance().getAPIManager().getMMOApi().getResponseAPI();
		this.updateTask = new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(new Runnable() {
			public void run() {
				MMOUpdateManager.this.executeUpdate();
			}
		}, 0, thresholdMillis, TimeUnit.MILLISECONDS);
	}

	public void addUserToUpdate(User user) {
		synchronized (this.usersToUpdate) {
			if (!this.usersToUpdate.contains(user))
				this.usersToUpdate.add(user);
		}
	}

	public void addBatchToUpdate(List<User> users) {
		synchronized (this.usersToUpdate) {
			for (User item : users)
				if (!this.usersToUpdate.contains(item))
					this.usersToUpdate.add(item);
		}
	}

	public void addItemToUpdate(BaseMMOItem item) {
		synchronized (this.itemsToUpdate) {
			if (!this.itemsToUpdate.contains(item))
				this.itemsToUpdate.add(item);
		}
	}

	public int getUpdateThreshold() {
		return this.threshold;
	}

	public void setUpdateThreshold(int millis) {
		this.threshold = millis;
	}

	public void destroy() {
		if (this.updateTask != null)
			this.updateTask.cancel(true);
	}

	private void executeUpdate() {
		try {
			if ((this.usersToUpdate.size() == 0) && (this.itemsToUpdate.size() == 0)) {
				return;
			}
			Set<User> allAffectedUsers = new HashSet<User>();

			List<User> usersToUpdateCopy = null;
			synchronized (this.usersToUpdate) {
				usersToUpdateCopy = new LinkedList<User>(this.usersToUpdate);
				this.usersToUpdate.clear();
			}

			usersToUpdateCopy.addAll(findUsersAffectedByItemsUpdate());
			for (Iterator<User> it = usersToUpdateCopy.iterator(); it.hasNext();) {
				User user = it.next();

				computeAndUpdateUsers(user, allAffectedUsers);
			}

			for (User affectedUser : allAffectedUsers) {
				boolean userWasNotAlreadyUpdated = !usersToUpdateCopy.contains(affectedUser);

				if (userWasNotAlreadyUpdated)
					computeAndUpdateUsers(affectedUser);
			}
		} catch (Exception e) {
		}
	}

	private void computeAndUpdateUsers(User user) {
		computeAndUpdateUsers(user, null);
	}

	private void computeAndUpdateUsers(User user, Set<User> allAffectedUsers) {
		if (user.getCurrentMMORoom() != this.mmoRoom) {
			return;
		}

		List<User> newProxyList = this.mmoRoom.getProximityManager().getProximityList(user);

		List<BaseMMOItem> newItemsList = this.mmoRoom.getItemsManager().getItemList(user);

		if ((newProxyList == null) || (newItemsList == null)) {
			return;
		}

		List<User> plusUserList = new LinkedList<User>(newProxyList);
		List<User> minusUserList = user.getLastProxyList();
		List<BaseMMOItem> plusItemList = new LinkedList<BaseMMOItem>(newItemsList);
		List<BaseMMOItem> minusItemList = user.getLastMMOItemsList();

		user.setLastProxyList(newProxyList);
		user.setLastMMOItemsList(newItemsList);

		boolean previousUserListExists = minusUserList != null;
		boolean previousItemListExists = minusItemList != null;

		if (allAffectedUsers != null) {
			allAffectedUsers.addAll(plusUserList);

			if (previousUserListExists) {
				allAffectedUsers.addAll(minusUserList);
			}

		}

		if (previousUserListExists) {
			for (Iterator<User> it = minusUserList.iterator(); it.hasNext();) {
				User item = it.next();

				if (!plusUserList.contains(item)) {
					continue;
				}
				it.remove();
				plusUserList.remove(item);
			}

		}

		if (previousItemListExists) {
			for (Iterator<BaseMMOItem> it = minusItemList.iterator(); it.hasNext();) {
				BaseMMOItem item = it.next();

				if (!plusItemList.contains(item)) {
					continue;
				}
				it.remove();
				plusItemList.remove(item);
			}

		}

		boolean needsUpdate = (plusUserList != null) && (plusUserList.size() > 0);
		needsUpdate |= ((minusUserList != null) && (minusUserList.size() > 0));
		needsUpdate |= ((plusItemList != null) && (plusItemList.size() > 0));
		needsUpdate |= ((minusItemList != null) && (minusItemList.size() > 0));

		if (needsUpdate) {
			this.responseAPI.notifyProximityListUpdate(this.mmoRoom,
					new MMOUpdateDelta(user, plusUserList, minusUserList, plusItemList, minusItemList));
		}
	}

	private Set<User> findUsersAffectedByItemsUpdate() {
		Set<User> affectedUsers = new HashSet<User>();

		List<BaseMMOItem> itemsToUpdateCopy = null;
		synchronized (this.itemsToUpdate) {
			itemsToUpdateCopy = new LinkedList<BaseMMOItem>(this.itemsToUpdate);
			this.itemsToUpdate.clear();
		}

		for (BaseMMOItem item : itemsToUpdateCopy) {
			affectedUsers.addAll(findUsersAffectedByThisItem(item));
		}

		return affectedUsers;
	}

	private Collection<User> findUsersAffectedByThisItem(BaseMMOItem item) {
		Set<User> userList = new HashSet<User>();
		List<User> oldUsers = item.getLastProxyList();

		if (oldUsers != null) {
			userList.addAll(oldUsers);
		}
		List<User> newUsers = this.mmoRoom.getProximityManager().getProximityList(item.getLastLocation());

		userList.addAll(newUsers);

		return userList;
	}
}