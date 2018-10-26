package com.net.server.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GameArrayLite extends GameArray {
	public static GameArrayLite newInstance() {
		return new GameArrayLite();
	}

	public Byte getByte(int index) {
		Integer i = super.getInt(index);

		return i != null ? Byte.valueOf(i.byteValue()) : null;
	}

	public Short getShort(int index) {
		Integer i = super.getInt(index);

		return i != null ? Short.valueOf(i.shortValue()) : null;
	}

	public Float getFloat(int index) {
		Double d = super.getDouble(index);

		return d != null ? Float.valueOf(d.floatValue()) : null;
	}

	public Collection<Boolean> getBoolArray(int key) {
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

	public Collection<Short> getShortArray(int key) {
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

	public Collection<Integer> getIntArray(int key) {
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

	public Collection<Float> getFloatArray(int key) {
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

	public Collection<Double> getDoubleArray(int key) {
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

	public Collection<String> getUtfStringArray(int key) {
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