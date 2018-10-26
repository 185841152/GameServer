package com.net.server.mmo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.server.entities.User;

public class ProximityManager implements IProximityManager {
	private final int SECTOR_SIZE_MULTIPLIER = 1;
	private final Logger log;
	private final Vec3D aoi;
	private final Vec3D sectorSize;
	private final ConcurrentMap<P3D, ConcurrentLinkedQueue<User>> map;

	public ProximityManager(Vec3D aoi) {
		this.log = LoggerFactory.getLogger(getClass());
		this.aoi = aoi;

		if (aoi.isFloat()) {
			this.sectorSize = new Vec3D(aoi.floatX() * 1.0F, aoi.floatY() * 1.0F, aoi.floatZ() * 1.0F);
		} else {
			this.sectorSize = new Vec3D(aoi.intX() * SECTOR_SIZE_MULTIPLIER, aoi.intY() * SECTOR_SIZE_MULTIPLIER,
					aoi.intZ() * SECTOR_SIZE_MULTIPLIER);
		}

		this.map = new ConcurrentHashMap<P3D, ConcurrentLinkedQueue<User>>();
	}

	public Vec3D getDefaultAOI() {
		return this.aoi;
	}

	public void addUser(User user) {
		P3D pos = findSector(user);
		user.setProperty("_uPos", pos);

		moveUser(user, pos, null);
	}

	private void moveUser(User user, P3D newPos, P3D oldPos) {
		ConcurrentLinkedQueue<User> uList = null;

		synchronized (this.map) {
			uList = this.map.get(newPos);

			if (uList == null) {
				uList = new ConcurrentLinkedQueue<User>();
				this.map.put(newPos, uList);
			}

		}

		uList.add(user);

		if (oldPos != null) {
			uList = this.map.get(oldPos);
			if (uList != null)
				uList.remove(user);
		}
	}

	public void updateUser(User user) {
		P3D newPos = findSector(user);

		P3D oldPos = (P3D) user.getProperty("_uPos");

		user.setProperty("_uPos", newPos);

		if (!newPos.equals(oldPos))
			moveUser(user, newPos, oldPos);
	}

	public List<User> getProximityList(User target, Vec3D aoi) {
		P3D targetPos = (P3D) target.getProperty("_uPos");
		Vec3D targetLocation = (Vec3D) target.getProperty("_uLoc");

		if (targetLocation == null) {
			return null;
		}
		List<P3D> queryBlocks = getQueryBlocks(targetPos);
		List<User> proximityList = new LinkedList<User>();

		for (P3D pos : queryBlocks) {
			ConcurrentLinkedQueue<User> users = this.map.get(pos);

			if (users == null) {
				continue;
			}
			for (User u : users) {
				if (u == target) {
					continue;
				}
				if (userFallsWithinAOI(u, targetLocation, aoi)) {
					proximityList.add(u);
				}
			}
		}
		return proximityList;
	}

	public List<User> getProximityList(User target) {
		return getProximityList(target, this.aoi);
	}

	public List<User> getProximityList(Vec3D pos) {
		return getProximityList(pos, this.aoi);
	}

	public List<User> getProximityList(Vec3D targetLocation, Vec3D aoi) {
		List<P3D> queryBlocks = getQueryBlocks(findSector(targetLocation));

		return findLocalItemsWithinAOI(queryBlocks, targetLocation, aoi);
	}

	private boolean userFallsWithinAOI(User userToCheck, Vec3D targetLocation, Vec3D aoi) {
		Vec3D checkLocation = (Vec3D) userToCheck.getProperty("_uLoc");

		if (checkLocation == null) {
			if (this.log.isDebugEnabled()) {
				this.log.debug("User: " + userToCheck + " has no location in the map.");
			}
			return false;
		}
		boolean checkZ;
		boolean checkX;
		boolean checkY;
		if (targetLocation.isFloat()) {
			checkX = Math.abs(targetLocation.floatX() - checkLocation.floatX()) < aoi.floatX();
			checkY = Math.abs(targetLocation.floatY() - checkLocation.floatY()) < aoi.floatY();

			checkZ = aoi.floatZ() == 0.0F;
		} else {
			checkX = Math.abs(targetLocation.intX() - checkLocation.intX()) < aoi.intX();
			checkY = Math.abs(targetLocation.intY() - checkLocation.intY()) < aoi.intY();

			checkZ = aoi.intZ() == 0;
		}

		return (checkX) && (checkY) && (checkZ);
	}

	private List<P3D> getQueryBlocks(P3D center) {
		List<P3D> queryBlocks = new LinkedList<P3D>();

		for (int z = -1; z <= 1; z++) {
			for (int y = -1; y <= 1; y++) {
				for (int x = -1; x <= 1; x++) {
					queryBlocks.add(new P3D(center.px + x, center.py + y, center.pz + z));
				}
			}
		}
		return queryBlocks;
	}

	public static void main(String[] args) {
		ProximityManager manager = new ProximityManager(new Vec3D(40, 30));
		List<P3D> p3d = manager.getQueryBlocks(new P3D(0, 0, 0));
		System.out.println(p3d.size());
		System.out.println(p3d);

	}

	public Vec3D getSectorSize() {
		return this.sectorSize;
	}

	public int getSize() {
		return this.map.size();
	}

	public void removeUser(User user) {
		P3D lastPos = (P3D) user.getProperty("_uPos");

		if (lastPos == null) {
			return;
		}
		ConcurrentLinkedQueue<User> q = this.map.get(lastPos);

		if ((q != null) && (!q.contains(user))) {
			lastPos = findUserLocation(user);
			q = this.map.get(lastPos);
		}

		if (q != null) {
			q.remove(user);

			user.removeProperty("_uPos");
		} else {
			throw new IllegalStateException();
		}
	}

	public P3D findUserLocation(User user) {
		for (Map.Entry<P3D, ConcurrentLinkedQueue<User>> entry : this.map.entrySet()) {
			if (entry.getValue().contains(user)) {
				return (P3D) entry.getKey();
			}
		}
		return null;
	}

	public List<User> dumpAllUsers() {
		List<User> allUsers = new LinkedList<User>();
		for (ConcurrentLinkedQueue<User> q : this.map.values()) {
			allUsers.addAll(q);
		}
		return allUsers;
	}

	private P3D findSector(User user) {
		Vec3D pos = (Vec3D) user.getProperty("_uLoc");
		return findSector(pos);
	}

	private P3D findSector(Vec3D pos) {
		if (pos == null) {
			throw new IllegalArgumentException("User does not have a position assigned!");
		}
		if (pos.isFloat() != this.sectorSize.isFloat()) {
			throw new IllegalArgumentException(
					"User coordinates don't match numeric type of the Room's Area Of Interest (AOI)");
		}
		if (pos.isFloat()) {
			return findFloatSector(pos);
		}
		return findIntSector(pos);
	}

	private P3D findFloatSector(Vec3D pos) {
		int xx = (int) (pos.floatX() / this.sectorSize.floatX());
		xx = pos.floatX() < 0.0F ? xx - 1 : xx;
		int yy = (int) (pos.floatY() / this.sectorSize.floatY());
		yy = pos.floatY() < 0.0F ? yy - 1 : yy;

		int zz = 0;

		if (this.sectorSize.floatZ() != 0.0F) {
			zz = (int) (pos.floatZ() / this.sectorSize.floatZ());
			zz = pos.floatZ() < 0.0F ? zz - 1 : zz;
		}

		return new P3D(xx, yy, zz);
	}

	private P3D findIntSector(Vec3D pos) {
		int xx = pos.intX() / this.sectorSize.intX();
		xx = pos.intX() < 0 ? xx - 1 : xx;
		int yy = pos.intY() / this.sectorSize.intY();
		yy = pos.intY() < 0 ? yy - 1 : yy;

		int zz = 0;

		if (this.sectorSize.intZ() != 0) {
			zz = pos.intZ() / this.sectorSize.intZ();
			zz = pos.intZ() < 0 ? zz - 1 : zz;
		}

		return new P3D(xx, yy, zz);
	}

	private List<User> findLocalItemsWithinAOI(List<P3D> queryBlocks, Vec3D targetLocation, Vec3D aoi) {
		List<User> userList = new LinkedList<User>();

		for (P3D pos : queryBlocks) {
			ConcurrentLinkedQueue<User> users = this.map.get(pos);

			if (users == null) {
				continue;
			}
			for (User user : users) {
				if (userFallsWithinAOI(user, targetLocation, aoi)) {
					userList.add(user);
				}
			}
		}
		return userList;
	}

	public void dumpState() {
		for (P3D pos : this.map.keySet()) {
			System.out.println(pos + " --> " + this.map.get(pos));
		}
	}
}