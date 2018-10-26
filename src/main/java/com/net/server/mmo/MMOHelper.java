package com.net.server.mmo;

import java.util.List;

import com.net.server.entities.User;

public class MMOHelper {
	public static final String USER_LOC = "_uLoc";
	public static final String USER_LAST_POS = "_uPos";
	public static final String USER_JOIN_TIME = "_uJoinTime";

	public static List<User> getProximitySessionList(User target) {
		if (target.getLastProxyList() == null) {
			return null;
		}
		return target.getLastProxyList();
	}

	public static List<User> getProximitySessionList(MMORoom theRoom, User target, Vec3D aoi) {
		if (target.getLastProxyList() == null) {
			return null;
		}
		List<User> customProxyList = theRoom.getProximityManager().getProximityList(target, aoi);

		return customProxyList;
	}

	public static Vec3D stringToVec3D(String values, boolean forceFloats) {
		Vec3D vec3d = null;

		if (!values.equals("")) {
			String[] items = values.split("\\,");
			if (items.length != 3) {
				throw new IllegalArgumentException("Cannot convert to Vec3D: " + values);
			}

			boolean isFloat = (values.indexOf('.') > -1) || (forceFloats);

			if (isFloat)
				vec3d = new Vec3D(Float.parseFloat(items[0]), Float.parseFloat(items[1]), Float.parseFloat(items[2]));
			else {
				vec3d = new Vec3D(Integer.parseInt(items[0]), Integer.parseInt(items[1]), Integer.parseInt(items[2]));
			}
		}
		return vec3d;
	}

	public static Vec3D getMMOItemLocation(BaseMMOItem item) {
		return item.getLastLocation();
	}

	public static P3D getMMOItemPos(BaseMMOItem item) {
		return item.getLastPos();
	}
}