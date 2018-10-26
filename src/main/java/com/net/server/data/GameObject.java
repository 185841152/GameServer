package com.net.server.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.net.server.util.ByteUtils;

public class GameObject implements IGameObject {
	private Map<String, GameDataWrapper> dataHolder;
	private IGameDataSerializer serializer;

	public static GameObject newFromObject(Object o) {
		return (GameObject) DefaultGameDataSerializer.getInstance().pojo2game(o);
	}

	public static GameObject newFromBinaryData(byte[] bytes) {
		return (GameObject) DefaultGameDataSerializer.getInstance().binary2object(bytes);
	}

	public static IGameObject newFromJsonData(String jsonStr) {
		return DefaultGameDataSerializer.getInstance().json2object(jsonStr);
	}

	public static GameObject newFromResultSet(ResultSet rset) throws SQLException {
		return DefaultGameDataSerializer.getInstance().resultSet2object(rset);
	}

	public static GameObject newInstance() {
		return new GameObject();
	}

	public GameObject() {
		this.dataHolder = new ConcurrentHashMap<String, GameDataWrapper>();
		this.serializer = DefaultGameDataSerializer.getInstance();
	}

	public Iterator<Map.Entry<String, GameDataWrapper>> iterator() {
		return this.dataHolder.entrySet().iterator();
	}

	public boolean containsKey(String key) {
		return this.dataHolder.containsKey(key);
	}

	public boolean removeElement(String key) {
		return this.dataHolder.remove(key) != null;
	}

	public int size() {
		return this.dataHolder.size();
	}

	public byte[] toBinary() {
		return this.serializer.object2binary(this);
	}

	public String toJson() {
		return this.serializer.object2json(flatten());
	}

	public String getDump() {
		if (size() == 0) {
			return "[ Empty GameObject ]";
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
		StringBuilder buffer = new StringBuilder();
		buffer.append('{');

		for (String key : getKeys()) {
			GameDataWrapper wrapper = get(key);
			buffer.append("(").append(wrapper.getTypeId().name().toLowerCase()).append(") ").append(key).append(": ");

			if (wrapper.getTypeId() == GameDataType.Game_OBJECT) {
				buffer.append(((GameObject) wrapper.getObject()).getDump(false));
			} else if (wrapper.getTypeId() == GameDataType.Game_ARRAY) {
				buffer.append(((GameArray) wrapper.getObject()).getDump(false));
			} else if (wrapper.getTypeId() == GameDataType.BYTE_ARRAY) {
				buffer.append(DefaultObjectDumpFormatter.prettyPrintByteArray((byte[]) wrapper.getObject()));
			} else if (wrapper.getTypeId() == GameDataType.CLASS) {
				buffer.append(wrapper.getObject().getClass().getName());
			} else {
				buffer.append(wrapper.getObject());
			}
			buffer.append(';');
		}

		buffer.append('}');

		return buffer.toString();
	}

	public String getHexDump() {
		return ByteUtils.fullHexDump(toBinary());
	}

	public boolean isNull(String key) {
		GameDataWrapper wrapper = (GameDataWrapper) this.dataHolder.get(key);

		if (wrapper == null) {
			return false;
		}
		return wrapper.getTypeId() == GameDataType.NULL;
	}

	public GameDataWrapper get(String key) {
		return (GameDataWrapper) this.dataHolder.get(key);
	}

	public Boolean getBool(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Boolean) o.getObject();
	}

	@SuppressWarnings("unchecked")
	public Collection<Boolean> getBoolArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Collection<Boolean>) o.getObject();
	}

	public Byte getByte(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Byte) o.getObject();
	}

	public byte[] getByteArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (byte[]) o.getObject();
	}

	public Double getDouble(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Double) o.getObject();
	}

	@SuppressWarnings("unchecked")
	public Collection<Double> getDoubleArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Collection<Double>) o.getObject();
	}

	public Float getFloat(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Float) o.getObject();
	}

	@SuppressWarnings("unchecked")
	public Collection<Float> getFloatArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Collection<Float>) o.getObject();
	}

	public Integer getInt(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Integer) o.getObject();
	}

	@SuppressWarnings("unchecked")
	public Collection<Integer> getIntArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Collection<Integer>) o.getObject();
	}

	public Set<String> getKeys() {
		return this.dataHolder.keySet();
	}

	public Long getLong(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Long) o.getObject();
	}

	@SuppressWarnings("unchecked")
	public Collection<Long> getLongArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Collection<Long>) o.getObject();
	}

	public IGameArray getGameArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (IGameArray) o.getObject();
	}

	public IGameObject getGameObject(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (IGameObject) o.getObject();
	}

	public Short getShort(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Short) o.getObject();
	}

	@SuppressWarnings("unchecked")
	public Collection<Short> getShortArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Collection<Short>) o.getObject();
	}

	public Integer getUnsignedByte(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return Integer
				.valueOf(DefaultGameDataSerializer.getInstance().getUnsignedByte(((Byte) o.getObject()).byteValue()));
	}

	public Collection<Integer> getUnsignedByteArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}

		DefaultGameDataSerializer serializer = DefaultGameDataSerializer.getInstance();
		Collection<Integer> intCollection = new ArrayList<Integer>();

		for (byte b : (byte[]) o.getObject()) {
			intCollection.add(Integer.valueOf(serializer.getUnsignedByte(b)));
		}

		return intCollection;
	}

	public String getUtfString(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (String) o.getObject();
	}

	@SuppressWarnings("unchecked")
	public Collection<String> getUtfStringArray(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return (Collection<String>) o.getObject();
	}

	public Object getClass(String key) {
		GameDataWrapper o = (GameDataWrapper) this.dataHolder.get(key);

		if (o == null) {
			return null;
		}
		return o.getObject();
	}

	public void putBool(String key, boolean value) {
		putObj(key, Boolean.valueOf(value), GameDataType.BOOL);
	}

	public void putBoolArray(String key, Collection<Boolean> value) {
		putObj(key, value, GameDataType.BOOL_ARRAY);
	}

	public void putByte(String key, byte value) {
		putObj(key, Byte.valueOf(value), GameDataType.BYTE);
	}

	public void putByteArray(String key, byte[] value) {
		putObj(key, value, GameDataType.BYTE_ARRAY);
	}

	public void putDouble(String key, double value) {
		putObj(key, Double.valueOf(value), GameDataType.DOUBLE);
	}

	public void putDoubleArray(String key, Collection<Double> value) {
		putObj(key, value, GameDataType.DOUBLE_ARRAY);
	}

	public void putFloat(String key, float value) {
		putObj(key, Float.valueOf(value), GameDataType.FLOAT);
	}

	public void putFloatArray(String key, Collection<Float> value) {
		putObj(key, value, GameDataType.FLOAT_ARRAY);
	}

	public void putInt(String key, int value) {
		putObj(key, Integer.valueOf(value), GameDataType.INT);
	}

	public void putIntArray(String key, Collection<Integer> value) {
		putObj(key, value, GameDataType.INT_ARRAY);
	}

	public void putLong(String key, long value) {
		putObj(key, Long.valueOf(value), GameDataType.LONG);
	}

	public void putLongArray(String key, Collection<Long> value) {
		putObj(key, value, GameDataType.LONG_ARRAY);
	}

	public void putNull(String key) {
		this.dataHolder.put(key, new GameDataWrapper(GameDataType.NULL, null));
	}

	public void putGameArray(String key, IGameArray value) {
		putObj(key, value, GameDataType.Game_ARRAY);
	}

	public void putGameObject(String key, IGameObject value) {
		putObj(key, value, GameDataType.Game_OBJECT);
	}

	public void putShort(String key, short value) {
		putObj(key, Short.valueOf(value), GameDataType.SHORT);
	}

	public void putShortArray(String key, Collection<Short> value) {
		putObj(key, value, GameDataType.SHORT_ARRAY);
	}

	public void putUtfString(String key, String value) {
		putObj(key, value, GameDataType.UTF_STRING);
	}

	public void putUtfStringArray(String key, Collection<String> value) {
		putObj(key, value, GameDataType.UTF_STRING_ARRAY);
	}

	public void put(String key, GameDataWrapper wrappedObject) {
		putObj(key, wrappedObject, null);
	}

	public void putClass(String key, Object o) {
		putObj(key, o, GameDataType.CLASS);
	}

	public String toString() {
		return "[GameObject, size: " + size() + "]";
	}

	private void putObj(String key, Object value, GameDataType typeId) {
		if (key == null) {
			throw new IllegalArgumentException("GameObject requires a non-null key for a 'put' operation!");
		}
		if (key.length() > 255) {
			throw new IllegalArgumentException("GameObject keys must be less than 255 characters!");
		}
		if (value == null) {
			throw new IllegalArgumentException(
					"GameObject requires a non-null value! If you need to add a null use the putNull() method.");
		}
		if ((value instanceof GameDataWrapper))
			this.dataHolder.put(key, (GameDataWrapper) value);
		else
			this.dataHolder.put(key, new GameDataWrapper(typeId, value));
	}

	private Map<String, Object> flatten() {
		Map<String, Object> map = new HashMap<String, Object>();
		DefaultGameDataSerializer.getInstance().flattenObject(map, this);

		return map;
	}
}