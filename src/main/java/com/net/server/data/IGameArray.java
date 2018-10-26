package com.net.server.data;

import java.util.Collection;
import java.util.Iterator;

public abstract interface IGameArray {
	public abstract boolean contains(Object paramObject);

	public abstract Iterator<GameDataWrapper> iterator();

	public abstract Object getElementAt(int paramInt);

	public abstract GameDataWrapper get(int paramInt);

	public abstract void removeElementAt(int paramInt);

	public abstract int size();

	public abstract byte[] toBinary();

	public abstract String toJson();

	public abstract String getHexDump();

	public abstract String getDump();

	public abstract String getDump(boolean paramBoolean);

	public abstract void addNull();

	public abstract void addBool(boolean paramBoolean);

	public abstract void addByte(byte paramByte);

	public abstract void addShort(short paramShort);

	public abstract void addInt(int paramInt);

	public abstract void addLong(long paramLong);

	public abstract void addFloat(float paramFloat);

	public abstract void addDouble(double paramDouble);

	public abstract void addUtfString(String paramString);

	public abstract void addBoolArray(Collection<Boolean> paramCollection);

	public abstract void addByteArray(byte[] paramArrayOfByte);

	public abstract void addShortArray(Collection<Short> paramCollection);

	public abstract void addIntArray(Collection<Integer> paramCollection);

	public abstract void addLongArray(Collection<Long> paramCollection);

	public abstract void addFloatArray(Collection<Float> paramCollection);

	public abstract void addDoubleArray(Collection<Double> paramCollection);

	public abstract void addUtfStringArray(Collection<String> paramCollection);

	public abstract void addGameArray(IGameArray paramIGameArray);

	public abstract void addGameObject(IGameObject paramIGameObject);

	public abstract void addClass(Object paramObject);

	public abstract void add(GameDataWrapper paramGameDataWrapper);

	public abstract boolean isNull(int paramInt);

	public abstract Boolean getBool(int paramInt);

	public abstract Byte getByte(int paramInt);

	public abstract Integer getUnsignedByte(int paramInt);

	public abstract Short getShort(int paramInt);

	public abstract Integer getInt(int paramInt);

	public abstract Long getLong(int paramInt);

	public abstract Float getFloat(int paramInt);

	public abstract Double getDouble(int paramInt);

	public abstract String getUtfString(int paramInt);

	public abstract Collection<Boolean> getBoolArray(int paramInt);

	public abstract byte[] getByteArray(int paramInt);

	public abstract Collection<Integer> getUnsignedByteArray(int paramInt);

	public abstract Collection<Short> getShortArray(int paramInt);

	public abstract Collection<Integer> getIntArray(int paramInt);

	public abstract Collection<Long> getLongArray(int paramInt);

	public abstract Collection<Float> getFloatArray(int paramInt);

	public abstract Collection<Double> getDoubleArray(int paramInt);

	public abstract Collection<String> getUtfStringArray(int paramInt);

	public abstract Object getClass(int paramInt);

	public abstract IGameArray getGameArray(int paramInt);

	public abstract IGameObject getGameObject(int paramInt);
}