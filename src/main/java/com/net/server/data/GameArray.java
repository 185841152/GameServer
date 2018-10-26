package com.net.server.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.net.server.util.ByteUtils;

public class GameArray implements IGameArray {
	private IGameDataSerializer serializer;
	private List<GameDataWrapper> dataHolder;

	public GameArray() {
		this.dataHolder = new ArrayList<GameDataWrapper>();
		this.serializer = DefaultGameDataSerializer.getInstance();
	}

	public static GameArray newFromBinaryData(byte[] bytes) {
		return (GameArray) DefaultGameDataSerializer.getInstance().binary2array(bytes);
	}

	public static GameArray newFromResultSet(ResultSet rset) throws SQLException {
		return DefaultGameDataSerializer.getInstance().resultSet2array(rset);
	}

	public static GameArray newFromJsonData(String jsonStr) {
		return (GameArray) DefaultGameDataSerializer.getInstance().json2array(jsonStr);
	}

	public static GameArray newInstance() {
		return new GameArray();
	}

	public String getDump() {
		if (size() == 0) {
			return "[ Empty GameArray ]";
		}
		return DefaultObjectDumpFormatter.prettyPrintDump(dump());
	}

	public String getDump(boolean noFormat) {
		if (!noFormat) {
			return dump();
		}
		return getDump();
	}

	private String dump() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		Object objDump = null;

		for (Iterator<GameDataWrapper> iter = this.dataHolder.iterator(); iter.hasNext();) {
			GameDataWrapper wrappedObject = (GameDataWrapper) iter.next();

			if (wrappedObject.getTypeId() == GameDataType.Game_OBJECT) {
				objDump = ((IGameObject) wrappedObject.getObject()).getDump(false);
			} else if (wrappedObject.getTypeId() == GameDataType.Game_ARRAY) {
				objDump = ((IGameArray) wrappedObject.getObject()).getDump(false);
			} else if (wrappedObject.getTypeId() == GameDataType.BYTE_ARRAY) {
				objDump = DefaultObjectDumpFormatter.prettyPrintByteArray((byte[]) wrappedObject.getObject());
			} else if (wrappedObject.getTypeId() == GameDataType.CLASS) {
				objDump = wrappedObject.getObject().getClass().getName();
			} else {
				objDump = wrappedObject.getObject();
			}
			sb.append(" (").append(wrappedObject.getTypeId().name().toLowerCase()).append(") ").append(objDump)
					.append(';');
		}

		if (size() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		sb.append('}');

		return sb.toString();
	}

	public String getHexDump() {
		return ByteUtils.fullHexDump(toBinary());
	}

	public byte[] toBinary() {
		return this.serializer.array2binary(this);
	}

	public String toJson() {
		return DefaultGameDataSerializer.getInstance().array2json(flatten());
	}

	public boolean isNull(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);

		if (wrapper == null) {
			return false;
		}
		return wrapper.getTypeId() == GameDataType.NULL;
	}

	public GameDataWrapper get(int index) {
		return (GameDataWrapper) this.dataHolder.get(index);
	}

	public Boolean getBool(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Boolean) wrapper.getObject() : null;
	}

	public Byte getByte(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Byte) wrapper.getObject() : null;
	}

	public Integer getUnsignedByte(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? Integer.valueOf(
				DefaultGameDataSerializer.getInstance().getUnsignedByte(((Byte) wrapper.getObject()).byteValue()))
				: null;
	}

	public Short getShort(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Short) wrapper.getObject() : null;
	}

	public Integer getInt(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Integer) wrapper.getObject() : null;
	}

	public Long getLong(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Long) wrapper.getObject() : null;
	}

	public Float getFloat(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Float) wrapper.getObject() : null;
	}

	public Double getDouble(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Double) wrapper.getObject() : null;
	}

	public String getUtfString(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (String) wrapper.getObject() : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Boolean> getBoolArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Collection<Boolean>) wrapper.getObject() : null;
	}

	public byte[] getByteArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (byte[]) wrapper.getObject() : null;
	}

	public Collection<Integer> getUnsignedByteArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);

		if (wrapper == null) {
			return null;
		}

		DefaultGameDataSerializer serializer = DefaultGameDataSerializer.getInstance();
		Collection<Integer> intCollection = new ArrayList<Integer>();

		for (byte b : (byte[]) wrapper.getObject()) {
			intCollection.add(Integer.valueOf(serializer.getUnsignedByte(b)));
		}

		return intCollection;
	}

	@SuppressWarnings("unchecked")
	public Collection<Short> getShortArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Collection<Short>) wrapper.getObject() : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Integer> getIntArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Collection<Integer>) wrapper.getObject() : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Long> getLongArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Collection<Long>) wrapper.getObject() : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Float> getFloatArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Collection<Float>) wrapper.getObject() : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Double> getDoubleArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Collection<Double>) wrapper.getObject() : null;
	}

	@SuppressWarnings("unchecked")
	public Collection<String> getUtfStringArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Collection<String>) wrapper.getObject() : null;
	}

	public IGameArray getGameArray(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (IGameArray) wrapper.getObject() : null;
	}

	public IGameObject getGameObject(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (IGameObject) wrapper.getObject() : null;
	}

	public Object getClass(int index) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? wrapper.getObject() : null;
	}

	public void addBool(boolean value) {
		addObject(Boolean.valueOf(value), GameDataType.BOOL);
	}

	public void addBoolArray(Collection<Boolean> value) {
		addObject(value, GameDataType.BOOL_ARRAY);
	}

	public void addByte(byte value) {
		addObject(Byte.valueOf(value), GameDataType.BYTE);
	}

	public void addByteArray(byte[] value) {
		addObject(value, GameDataType.BYTE_ARRAY);
	}

	public void addDouble(double value) {
		addObject(Double.valueOf(value), GameDataType.DOUBLE);
	}

	public void addDoubleArray(Collection<Double> value) {
		addObject(value, GameDataType.DOUBLE_ARRAY);
	}

	public void addFloat(float value) {
		addObject(Float.valueOf(value), GameDataType.FLOAT);
	}

	public void addFloatArray(Collection<Float> value) {
		addObject(value, GameDataType.FLOAT_ARRAY);
	}

	public void addInt(int value) {
		addObject(Integer.valueOf(value), GameDataType.INT);
	}

	public void addIntArray(Collection<Integer> value) {
		addObject(value, GameDataType.INT_ARRAY);
	}

	public void addLong(long value) {
		addObject(Long.valueOf(value), GameDataType.LONG);
	}

	public void addLongArray(Collection<Long> value) {
		addObject(value, GameDataType.LONG_ARRAY);
	}

	public void addNull() {
		addObject(null, GameDataType.NULL);
	}

	public void addGameArray(IGameArray value) {
		addObject(value, GameDataType.Game_ARRAY);
	}

	public void addGameObject(IGameObject value) {
		addObject(value, GameDataType.Game_OBJECT);
	}

	public void addShort(short value) {
		addObject(Short.valueOf(value), GameDataType.SHORT);
	}

	public void addShortArray(Collection<Short> value) {
		addObject(value, GameDataType.SHORT_ARRAY);
	}

	public void addUtfString(String value) {
		addObject(value, GameDataType.UTF_STRING);
	}

	public void addUtfStringArray(Collection<String> value) {
		addObject(value, GameDataType.UTF_STRING_ARRAY);
	}

	public void addClass(Object o) {
		addObject(o, GameDataType.CLASS);
	}

	public void add(GameDataWrapper wrappedObject) {
		this.dataHolder.add(wrappedObject);
	}

	public boolean contains(Object obj) {
		if (((obj instanceof IGameArray)) || ((obj instanceof IGameObject))) {
			throw new UnsupportedOperationException("IGameArray and IGameObject are not supported by this method.");
		}
		boolean found = false;

		for (Iterator<GameDataWrapper> iter = this.dataHolder.iterator(); iter.hasNext();) {
			Object item = iter.next().getObject();

			if (!item.equals(obj))
				continue;
			found = true;
			break;
		}

		return found;
	}

	public Object getElementAt(int index) {
		Object item = null;

		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(index);

		if (wrapper != null)
			;
		item = wrapper.getObject();

		return item;
	}

	public Iterator<GameDataWrapper> iterator() {
		return this.dataHolder.iterator();
	}

	public void removeElementAt(int index) {
		this.dataHolder.remove(index);
	}

	public int size() {
		return this.dataHolder.size();
	}

	public String toString() {
		return "[GameArray, size: " + size() + "]";
	}

	private void addObject(Object value, GameDataType typeId) {
		this.dataHolder.add(new GameDataWrapper(typeId, value));
	}

	private List<Object> flatten() {
		List<Object> list = new ArrayList<Object>();
		DefaultGameDataSerializer.getInstance().flattenArray(list, this);

		return list;
	}
}