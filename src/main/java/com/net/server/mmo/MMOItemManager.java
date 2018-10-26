package com.net.server.mmo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.net.server.entities.User;

public class MMOItemManager implements IMMOItemManager {
	private final int SECTOR_SIZE_MULTIPLIER = 1;
	private final Vec3D aoi;
	private final Vec3D sectorSize;
	private final ConcurrentMap<P3D, ConcurrentLinkedQueue<BaseMMOItem>> map;

	public MMOItemManager(Vec3D aoi) {
		this.aoi = aoi;

		if (aoi.isFloat()) {
			this.sectorSize = new Vec3D(aoi.floatX() * 1.0F, aoi.floatY() * 1.0F, aoi.floatZ() * 1.0F);
		} else {
			this.sectorSize = new Vec3D(aoi.intX() * SECTOR_SIZE_MULTIPLIER, aoi.intY() * SECTOR_SIZE_MULTIPLIER,
					aoi.intZ() * SECTOR_SIZE_MULTIPLIER);
		}

		this.map = new ConcurrentHashMap<P3D, ConcurrentLinkedQueue<BaseMMOItem>>();
	}

	public void setItem(BaseMMOItem item, Vec3D location) {
		P3D oldPos = item.getLastPos();
		boolean isNewItem = oldPos == null;

		P3D newPos = findSector(location);

		boolean isSamePosition = newPos.equals(item.getLastPos());

		item.setLastPos(newPos);

		item.setLastLocation(location);

		boolean needsUpdate = (isNewItem) || (!isSamePosition);

		if (needsUpdate)
			moveItem(item, newPos, oldPos);
	}

	public List<BaseMMOItem> getItemList(User target, Vec3D aoi) {
		P3D targetPos = (P3D) target.getProperty("_uPos");
		Vec3D targetLocation = (Vec3D) target.getProperty("_uLoc");

		if (targetLocation == null) {
			return null;
		}
		List<P3D> queryBlocks = getQueryBlocks(targetPos);

		return findLocalItemsWithinAOI(queryBlocks, targetLocation, aoi);
	}

	public List<BaseMMOItem> getItemList(User target) {
		return getItemList(target, this.aoi);
	}

	public List<BaseMMOItem> getItemList(Vec3D location, Vec3D aoi) {
		P3D targetPos = findSector(location);
		List<P3D> queryBlocks = getQueryBlocks(targetPos);

		return findLocalItemsWithinAOI(queryBlocks, location, aoi);
	}

	public List<BaseMMOItem> getItemList(Vec3D pos) {
		return getItemList(pos, this.aoi);
	}

	public int getSize() {
		return this.map.size();
	}

	public void removeItem(BaseMMOItem item) {
		P3D lastPos = item.getLastPos();

		if (lastPos == null) {
			return;
		}
		ConcurrentLinkedQueue<BaseMMOItem> q = this.map.get(lastPos);

		if ((q != null) && (!q.contains(item))) {
			lastPos = findItemLocation(item);
			q = this.map.get(lastPos);
		}

		if (q != null) {
			q.remove(item);
		} else
			throw new IllegalStateException();
	}

	public P3D findItemLocation(BaseMMOItem item) {
		for (Map.Entry<P3D, ConcurrentLinkedQueue<BaseMMOItem>> entry : this.map.entrySet()) {
			if (entry.getValue().contains(item)) {
				return (P3D) entry.getKey();
			}
		}
		return null;
	}

	private void moveItem(BaseMMOItem item, P3D newPos, P3D oldPos) {
		ConcurrentLinkedQueue<BaseMMOItem> itemList = null;

		synchronized (this.map) {
			itemList = this.map.get(newPos);

			if (itemList == null) {
				itemList = new ConcurrentLinkedQueue<BaseMMOItem>();
				this.map.put(newPos, itemList);
			}

		}

		itemList.add(item);

		if (oldPos != null) {
			itemList = this.map.get(oldPos);
			if (itemList != null)
				itemList.remove(item);
		}
	}

	private P3D findSector(Vec3D pos) {
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

	private boolean itemFallsWithinAOI(BaseMMOItem item, Vec3D targetLocation, Vec3D aoi) {
		Vec3D checkLocation = item.getLastLocation();
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

	private List<BaseMMOItem> findLocalItemsWithinAOI(List<P3D> queryBlocks, Vec3D targetLocation, Vec3D aoi) {
		List<BaseMMOItem> itemList = new LinkedList<BaseMMOItem>();

		for (P3D pos : queryBlocks) {
			ConcurrentLinkedQueue<BaseMMOItem> items = this.map.get(pos);

			if (items == null) {
				continue;
			}
			for (BaseMMOItem it : items) {
				if (itemFallsWithinAOI(it, targetLocation, aoi)) {
					itemList.add(it);
				}
			}
		}
		return itemList;
	}

	public void dumpState() {
		for (P3D pos : this.map.keySet()) {
			System.out.println(pos + " --> " + this.map.get(pos));
		}
	}
}