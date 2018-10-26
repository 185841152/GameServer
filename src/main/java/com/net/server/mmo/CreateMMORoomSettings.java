package com.net.server.mmo;

import com.net.server.api.CreateRoomSettings;

public class CreateMMORoomSettings extends CreateRoomSettings {
	private Vec3D defaultAOI;
	private MapLimits mapLimits;
	private int userMaxLimboSeconds = 0;
	private int proximityListUpdateMillis = 250;
	private boolean sendAOIEntryPoint = true;

	public Vec3D getDefaultAOI() {
		return this.defaultAOI;
	}

	public void setDefaultAOI(Vec3D defaultAOI) {
		this.defaultAOI = defaultAOI;
	}

	public MapLimits getMapLimits() {
		return this.mapLimits;
	}

	public void setMapLimits(MapLimits mapLimits) {
		this.mapLimits = mapLimits;
	}

	public int getUserMaxLimboSeconds() {
		return this.userMaxLimboSeconds;
	}

	public void setUserMaxLimboSeconds(int userMaxLimboSeconds) {
		this.userMaxLimboSeconds = userMaxLimboSeconds;
	}

	public int getProximityListUpdateMillis() {
		return this.proximityListUpdateMillis;
	}

	public void setProximityListUpdateMillis(int updateMillis) {
		this.proximityListUpdateMillis = updateMillis;
	}

	public boolean isSendAOIEntryPoint() {
		return this.sendAOIEntryPoint;
	}

	public void setSendAOIEntryPoint(boolean sendAOIEntryPoint) {
		this.sendAOIEntryPoint = sendAOIEntryPoint;
	}

	public static final class MapLimits {
		private Vec3D lowerLimit;
		private Vec3D higherLimit;

		public MapLimits(Vec3D low, Vec3D high) {
			if ((low != null) && (high != null)) {
				this.lowerLimit = low;
				this.higherLimit = high;
			} else {
				throw new IllegalArgumentException("Map Limits arguments must be both non null!");
			}
		}

		public Vec3D getLowerLimit() {
			return this.lowerLimit;
		}

		public Vec3D getHigherLimit() {
			return this.higherLimit;
		}
	}
}