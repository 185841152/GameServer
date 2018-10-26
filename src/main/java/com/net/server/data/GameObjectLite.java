package com.net.server.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class GameObjectLite extends GameObject {
	public static GameObject newInstance() {
		return new GameObjectLite();
	}

	public Byte getByte(String key) {
		Integer i = super.getInt(key);

		return i != null ? Byte.valueOf(i.byteValue()) : null;
	}

	public Short getShort(String key) {
		Integer i = super.getInt(key);

		return i != null ? Short.valueOf(i.shortValue()) : null;
	}

	public Float getFloat(String key) {
		Double d = super.getDouble(key);

		return d != null ? Float.valueOf(d.floatValue()) : null;
	}

	public Collection<Boolean> getBoolArray(String key) {
		IGameArray arr = getGameArray(key);
		if (arr == null) {
			return null;
		}
		List<Boolean> data = new ArrayList<Boolean>();

		for (int i = 0; i < arr.size(); i++) {
			data.add(arr.getBool(i));
		}
		return data;
	}

	public Collection<Short> getShortArray(String key) {
		IGameArray arr = getGameArray(key);
		if (arr == null) {
			return null;
		}
		List<Short> data = new ArrayList<Short>();

		for (int i = 0; i < arr.size(); i++) {
			data.add(Short.valueOf(arr.getInt(i).shortValue()));
		}
		return data;
	}

	public Collection<Integer> getIntArray(String key) {
		IGameArray arr = getGameArray(key);
		if (arr == null) {
			return null;
		}
		List<Integer> data = new ArrayList<Integer>();

		for (int i = 0; i < arr.size(); i++) {
			data.add(arr.getInt(i));
		}
		return data;
	}

	public Collection<Float> getFloatArray(String key) {
		IGameArray arr = getGameArray(key);
		if (arr == null) {
			return null;
		}
		List<Float> data = new ArrayList<Float>();

		for (int i = 0; i < arr.size(); i++) {
			data.add(Float.valueOf(arr.getDouble(i).floatValue()));
		}
		return data;
	}

	public Collection<Double> getDoubleArray(String key) {
		IGameArray arr = getGameArray(key);
		if (arr == null) {
			return null;
		}
		List<Double> data = new ArrayList<Double>();

		for (int i = 0; i < arr.size(); i++) {
			data.add(arr.getDouble(i));
		}
		return data;
	}

	public Collection<String> getUtfStringArray(String key) {
		IGameArray arr = getGameArray(key);
		if (arr == null) {
			return null;
		}
		List<String> data = new ArrayList<String>();

		for (int i = 0; i < arr.size(); i++) {
			data.add(arr.getUtfString(i));
		}
		return data;
	}
}