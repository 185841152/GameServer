package com.net.server.data;

import com.net.server.exceptions.CodecException;
import com.net.server.exceptions.GameRuntimeException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class DefaultGameDataSerializer implements IGameDataSerializer {
	private static final String CLASS_MARKER_KEY = "$C";
	private static final String CLASS_FIELDS_KEY = "$F";
	private static final String FIELD_NAME_KEY = "N";
	private static final String FIELD_VALUE_KEY = "V";
	private static DefaultGameDataSerializer instance = new DefaultGameDataSerializer();
	private static int BUFFER_CHUNK_SIZE = 512;
	private final Logger logger = LoggerFactory.getLogger(DefaultGameDataSerializer.class);
	private Collection<?> collection;

	public static DefaultGameDataSerializer getInstance() {
		return instance;
	}

	public int getUnsignedByte(byte b) {
		return 0xFF & b;
	}

	public String array2json(List<Object> array) {
		return JSONArray.fromObject(array).toString();
	}

	public IGameArray binary2array(byte[] data) {
		if (data.length < 3) {
			throw new IllegalStateException(
					"Can't decode an GameArray. Byte data is insufficient. Size: " + data.length + " bytes");
		}
		ByteBuf buffer = Unpooled.buffer(data.length);
		buffer.writeBytes(data);

		return decodeGameArray(buffer);
	}

	private IGameArray decodeGameArray(ByteBuf buffer) {
		IGameArray gameArray = GameArray.newInstance();

		byte headerBuffer = buffer.readByte();

		if (headerBuffer != GameDataType.Game_ARRAY.getTypeID()) {
			throw new IllegalStateException("Invalid GameDataType. Expected: " + GameDataType.Game_ARRAY.getTypeID()
					+ ", found: " + headerBuffer);
		}

		short size = buffer.readShort();

		if (size < 0) {
			throw new IllegalStateException("Can't decode GameArray. Size is negative = " + size);
		}

		try {
			for (int i = 0; i < size; i++) {
				GameDataWrapper decodedObject = decodeObject(buffer);

				if (decodedObject != null)
					gameArray.add(decodedObject);
				else {
					throw new IllegalStateException("Could not decode GameArray item at index: " + i);
				}
			}

		} catch (CodecException codecError) {
			throw new IllegalArgumentException(codecError.getMessage());
		}

		return gameArray;
	}

	public IGameObject binary2object(byte[] data) {
		if (data.length < 3) {
			throw new IllegalStateException(
					"Can't decode an GameObject. Byte data is insufficient. Size: " + data.length + " bytes");
		}
		ByteBuf buffer = Unpooled.buffer(data.length);
		buffer.writeBytes(data);

		return decodeGameObject(buffer);
	}

	private IGameObject decodeGameObject(ByteBuf buffer) {
		IGameObject gameObject = GameObject.newInstance();

		byte headerBuffer = buffer.readByte();

		if (headerBuffer != GameDataType.Game_OBJECT.getTypeID()) {
			throw new IllegalStateException("Invalid GameDataType. Expected: " + GameDataType.Game_OBJECT.getTypeID()
					+ ", found: " + headerBuffer);
		}

		short size = buffer.readShort();

		if (size < 0) {
			throw new IllegalStateException("Can't decode GameObject. Size is negative = " + size);
		}
		try {
			for (int i = 0; i < size; i++) {
				short keySize = buffer.readShort();
				if ((keySize < 0) || (keySize > 255)) {
					throw new IllegalStateException("Invalid GameObject key length. Found = " + keySize);
				}
				byte[] keyData = new byte[keySize];
				buffer.readBytes(keyData, 0, keyData.length);
				String key = new String(keyData);

				GameDataWrapper decodedObject = decodeObject(buffer);

				if (decodedObject != null)
					gameObject.put(key, decodedObject);
				else {
					throw new IllegalStateException("Could not decode value for key: " + keyData);
				}
			}

		} catch (CodecException codecError) {
			throw new IllegalArgumentException(codecError.getMessage());
		}

		return gameObject;
	}

	public IGameArray json2array(String jsonStr) {
		if (jsonStr.length() < 2) {
			throw new IllegalStateException(
					"Can't decode GameObject. JSON String is too short. Len: " + jsonStr.length());
		}
		JSONArray jsa = JSONArray.fromObject(jsonStr);

		return decodeGameArray(jsa);
	}

	@SuppressWarnings("rawtypes")
	private IGameArray decodeGameArray(JSONArray jsa) {
		IGameArray gameArray = GameArrayLite.newInstance();
		for (Iterator iter = jsa.iterator(); iter.hasNext();) {
			Object value = iter.next();
			GameDataWrapper decodedObject = decodeJsonObject(value);

			if (decodedObject != null)
				gameArray.add(decodedObject);
			else {
				throw new IllegalStateException("(json2sfarray) Could not decode value for object: " + value);
			}
		}
		return gameArray;
	}

	public IGameObject json2object(String jsonStr) {
		if (jsonStr.length() < 2) {
			throw new IllegalStateException(
					"Can't decode GameObject. JSON String is too short. Len: " + jsonStr.length());
		}

		JSONObject jso = JSONObject.fromObject(jsonStr);

		return decodeGameObject(jso);
	}

	@SuppressWarnings("rawtypes")
	private IGameObject decodeGameObject(JSONObject jso) {
		IGameObject gameObject = GameObjectLite.newInstance();
		for (Iterator localIterator = jso.keySet().iterator(); localIterator.hasNext();) {
			Object key = localIterator.next();

			Object value = jso.get(key);

			GameDataWrapper decodedObject = decodeJsonObject(value);

			if (decodedObject != null)
				gameObject.put((String) key, decodedObject);
			else {
				throw new IllegalStateException("(json2gameobj) Could not decode value for key: " + key);
			}
		}
		return gameObject;
	}

	private GameDataWrapper decodeJsonObject(Object o) {
		if ((o instanceof Integer)) {
			return new GameDataWrapper(GameDataType.INT, o);
		}

		if ((o instanceof Long)) {
			return new GameDataWrapper(GameDataType.LONG, o);
		}

		if ((o instanceof Double)) {
			return new GameDataWrapper(GameDataType.DOUBLE, o);
		}

		if ((o instanceof Boolean)) {
			return new GameDataWrapper(GameDataType.BOOL, o);
		}

		if ((o instanceof String)) {
			return new GameDataWrapper(GameDataType.UTF_STRING, o);
		}

		if ((o instanceof JSONObject)) {
			JSONObject jso = (JSONObject) o;

			if (jso.isNullObject()) {
				return new GameDataWrapper(GameDataType.NULL, null);
			}

			return new GameDataWrapper(GameDataType.Game_OBJECT, decodeGameObject(jso));
		}

		if ((o instanceof JSONArray)) {
			return new GameDataWrapper(GameDataType.Game_ARRAY, decodeGameArray((JSONArray) o));
		}

		throw new IllegalArgumentException(
				String.format("Unrecognized DataType while converting JSONObject 2 GameObject. Object: %s, Type: %s",
						new Object[] { o, o == null ? "null" : o.getClass() }));
	}

	public GameObject resultSet2object(ResultSet rset) throws SQLException {
		ResultSetMetaData metaData = rset.getMetaData();
		GameObject gameo = new GameObject();

		if (rset.isBeforeFirst()) {
			rset.next();
		}
		for (int col = 1; col <= metaData.getColumnCount(); col++) {
			String colName = metaData.getColumnName(col);
			int type = metaData.getColumnType(col);

			Object rawDataObj = rset.getObject(col);
			if (rawDataObj == null) {
				continue;
			}
			if (type == 0) {
				gameo.putNull(colName);
			} else if (type == 16) {
				gameo.putBool(colName, rset.getBoolean(col));
			} else if (type == 91) {
				gameo.putLong(colName, rset.getDate(col).getTime());
			} else if ((type == 6) || (type == 3) || (type == 8) || (type == 7)) {
				gameo.putDouble(colName, rset.getDouble(col));
			} else if ((type == 4) || (type == -6) || (type == 5)) {
				gameo.putInt(colName, rset.getInt(col));
			} else if ((type == -1) || (type == 12) || (type == 1)) {
				gameo.putUtfString(colName, rset.getString(col));
			} else if ((type == -9) || (type == -16) || (type == -15)) {
				gameo.putUtfString(colName, rset.getNString(col));
			} else if (type == 93) {
				gameo.putLong(colName, rset.getTimestamp(col).getTime());
			} else if (type == -5) {
				gameo.putLong(colName, rset.getLong(col));
			} else if (type == -4) {
				byte[] binData = getBlobData(colName, rset.getBinaryStream(col));

				if (binData != null) {
					gameo.putByteArray(colName, binData);
				}
			} else if (type == 2004) {
				Blob blob = rset.getBlob(col);
				gameo.putByteArray(colName, blob.getBytes(0L, (int) blob.length()));
			} else {
				logger.info("Skipping Unsupported SQL TYPE: " + type + ", Column:" + colName);
			}
		}

		return gameo;
	}

	private byte[] getBlobData(String colName, InputStream stream) {
		BufferedInputStream bis = new BufferedInputStream(stream);
		byte[] bytes = null;
		try {
			bytes = new byte[bis.available()];
			bis.read(bytes);
		} catch (IOException ex) {
			logger.warn("GameObject serialize error. Failed reading BLOB data for column: " + colName);
		} finally {
			IOUtils.closeQuietly(bis);
		}

		return bytes;
	}

	public GameArray resultSet2array(ResultSet rset) throws SQLException {
		GameArray gamea = new GameArray();

		while (rset.next()) {
			gamea.addGameObject(resultSet2object(rset));
		}

		return gamea;
	}

	public byte[] object2binary(IGameObject object) {
		ByteBuf buffer = Unpooled.buffer(BUFFER_CHUNK_SIZE);
		buffer.writeByte((byte) GameDataType.Game_OBJECT.getTypeID());
		buffer.writeShort((short) object.size());

		return obj2bin(object, buffer);
	}

	private byte[] obj2bin(IGameObject object, ByteBuf buffer) {
		Set<String> keys = object.getKeys();

		for (String key : keys) {
			GameDataWrapper wrapper = object.get(key);
			Object dataObj = wrapper.getObject();

			buffer = encodeGameObjectKey(buffer, key);

			buffer = encodeObject(buffer, wrapper.getTypeId(), dataObj);
		}

		int pos = buffer.writerIndex();

		byte[] result = new byte[pos];

		buffer.readBytes(result, 0, pos);

		return result;
	}

	public byte[] array2binary(IGameArray array) {
		ByteBuf buffer = Unpooled.buffer(BUFFER_CHUNK_SIZE);
		buffer.writeByte((byte) GameDataType.Game_ARRAY.getTypeID());
		buffer.writeShort((short) array.size());

		return arr2bin(array, buffer);
	}

	private byte[] arr2bin(IGameArray array, ByteBuf buffer) {
		Iterator<GameDataWrapper> iter = array.iterator();

		while (iter.hasNext()) {
			GameDataWrapper wrapper = iter.next();
			Object dataObj = wrapper.getObject();

			buffer = encodeObject(buffer, wrapper.getTypeId(), dataObj);
		}

		int pos = buffer.writerIndex();

		byte[] result = new byte[pos];

		buffer.readBytes(result, 0, pos);

		return result;
	}

	public String object2json(Map<String, Object> map) {
		return JSONObject.fromObject(map).toString();
	}

	public void flattenObject(Map<String, Object> map, GameObject gameObj) {
		for (Iterator<Entry<String, GameDataWrapper>> it = gameObj.iterator(); it.hasNext();) {
			Entry<String, GameDataWrapper> entry = it.next();

			String key = (String) entry.getKey();
			GameDataWrapper value = (GameDataWrapper) entry.getValue();

			if (value.getTypeId() == GameDataType.Game_OBJECT) {
				Map<String, Object> newMap = new HashMap<String, Object>();

				map.put(key, newMap);

				flattenObject(newMap, (GameObject) value.getObject());
			} else if (value.getTypeId() == GameDataType.Game_ARRAY) {
				List<Object> newList = new ArrayList<Object>();
				map.put(key, newList);
				flattenArray(newList, (GameArray) value.getObject());
			} else {
				map.put(key, value.getObject());
			}
		}
	}

	public void flattenArray(List<Object> array, GameArray gameArray) {
		for (Iterator<GameDataWrapper> it = gameArray.iterator(); it.hasNext();) {
			GameDataWrapper value = (GameDataWrapper) it.next();

			if (value.getTypeId() == GameDataType.Game_OBJECT) {
				Map<String, Object> newMap = new HashMap<String, Object>();

				array.add(newMap);

				flattenObject(newMap, (GameObject) value.getObject());
			} else if (value.getTypeId() == GameDataType.Game_ARRAY) {
				List<Object> newList = new ArrayList<Object>();
				array.add(newList);
				flattenArray(newList, (GameArray) value.getObject());
			} else {
				array.add(value.getObject());
			}
		}
	}

	private GameDataWrapper decodeObject(ByteBuf buffer) throws CodecException {
		GameDataWrapper decodedObject = null;

		byte headerByte = buffer.readByte();

		if (headerByte == GameDataType.NULL.getTypeID()) {
			decodedObject = binDecode_NULL(buffer);
		} else if (headerByte == GameDataType.BOOL.getTypeID()) {
			decodedObject = binDecode_BOOL(buffer);
		} else if (headerByte == GameDataType.BOOL_ARRAY.getTypeID()) {
			decodedObject = binDecode_BOOL_ARRAY(buffer);
		} else if (headerByte == GameDataType.BYTE.getTypeID()) {
			decodedObject = binDecode_BYTE(buffer);
		} else if (headerByte == GameDataType.BYTE_ARRAY.getTypeID()) {
			decodedObject = binDecode_BYTE_ARRAY(buffer);
		} else if (headerByte == GameDataType.SHORT.getTypeID()) {
			decodedObject = binDecode_SHORT(buffer);
		} else if (headerByte == GameDataType.SHORT_ARRAY.getTypeID()) {
			decodedObject = binDecode_SHORT_ARRAY(buffer);
		} else if (headerByte == GameDataType.INT.getTypeID()) {
			decodedObject = binDecode_INT(buffer);
		} else if (headerByte == GameDataType.INT_ARRAY.getTypeID()) {
			decodedObject = binDecode_INT_ARRAY(buffer);
		} else if (headerByte == GameDataType.LONG.getTypeID()) {
			decodedObject = binDecode_LONG(buffer);
		} else if (headerByte == GameDataType.LONG_ARRAY.getTypeID()) {
			decodedObject = binDecode_LONG_ARRAY(buffer);
		} else if (headerByte == GameDataType.FLOAT.getTypeID()) {
			decodedObject = binDecode_FLOAT(buffer);
		} else if (headerByte == GameDataType.FLOAT_ARRAY.getTypeID()) {
			decodedObject = binDecode_FLOAT_ARRAY(buffer);
		} else if (headerByte == GameDataType.DOUBLE.getTypeID()) {
			decodedObject = binDecode_DOUBLE(buffer);
		} else if (headerByte == GameDataType.DOUBLE_ARRAY.getTypeID()) {
			decodedObject = binDecode_DOUBLE_ARRAY(buffer);
		} else if (headerByte == GameDataType.UTF_STRING.getTypeID()) {
			decodedObject = binDecode_UTF_STRING(buffer);
		} else if (headerByte == GameDataType.UTF_STRING_ARRAY.getTypeID()) {
			decodedObject = binDecode_UTF_STRING_ARRAY(buffer);
		} else if (headerByte == GameDataType.Game_ARRAY.getTypeID()) {
			buffer.readerIndex(buffer.readerIndex() - 1);

			decodedObject = new GameDataWrapper(GameDataType.Game_ARRAY, decodeGameArray(buffer));
		} else if (headerByte == GameDataType.Game_OBJECT.getTypeID()) {
			buffer.readerIndex(buffer.readerIndex() - 1);

			IGameObject gameObj = decodeGameObject(buffer);
			GameDataType type = GameDataType.Game_OBJECT;
			Object finalSfsObj = gameObj;

			if ((gameObj.containsKey(CLASS_MARKER_KEY)) && (gameObj.containsKey(CLASS_FIELDS_KEY))) {
				type = GameDataType.CLASS;
				finalSfsObj = game2pojo(gameObj);
			}

			decodedObject = new GameDataWrapper(type, finalSfsObj);
		} else {
			throw new CodecException("Unknow GameDataType ID: " + headerByte);
		}
		return decodedObject;
	}

	@SuppressWarnings("unchecked")
	private ByteBuf encodeObject(ByteBuf buffer, GameDataType typeId, Object object) {
		switch (typeId) {
		case NULL:
			buffer = binEncode_NULL(buffer);
			break;
		case BOOL:
			buffer = binEncode_BOOL(buffer, (Boolean) object);
			break;
		case BYTE:
			buffer = binEncode_BYTE(buffer, (Byte) object);
			break;
		case SHORT:
			buffer = binEncode_SHORT(buffer, (Short) object);
			break;
		case INT:
			buffer = binEncode_INT(buffer, (Integer) object);
			break;
		case LONG:
			buffer = binEncode_LONG(buffer, (Long) object);
			break;
		case FLOAT:
			buffer = binEncode_FLOAT(buffer, (Float) object);
			break;
		case DOUBLE:
			buffer = binEncode_DOUBLE(buffer, (Double) object);
			break;
		case UTF_STRING:
			buffer = binEncode_UTF_STRING(buffer, (String) object);
			break;
		case BOOL_ARRAY:
			buffer = binEncode_BOOL_ARRAY(buffer, (Collection<Boolean>) object);
			break;
		case BYTE_ARRAY:
			buffer = binEncode_BYTE_ARRAY(buffer, (byte[]) object);
			break;
		case SHORT_ARRAY:
			buffer = binEncode_SHORT_ARRAY(buffer, (Collection<Short>) object);
			break;
		case INT_ARRAY:
			buffer = binEncode_INT_ARRAY(buffer, (Collection<Integer>) object);
			break;
		case LONG_ARRAY:
			buffer = binEncode_LONG_ARRAY(buffer, (Collection<Long>) object);
			break;
		case FLOAT_ARRAY:
			buffer = binEncode_FLOAT_ARRAY(buffer, (Collection<Float>) object);
			break;
		case DOUBLE_ARRAY:
			buffer = binEncode_DOUBLE_ARRAY(buffer, (Collection<Double>) object);
			break;
		case UTF_STRING_ARRAY:
			buffer = binEncode_UTF_STRING_ARRAY(buffer, (Collection<String>) object);
			break;
		case Game_ARRAY:
			buffer = addData(buffer, array2binary((GameArray) object));
			break;
		case Game_OBJECT:
			buffer = addData(buffer, object2binary((GameObject) object));
			break;
		case CLASS:
			buffer = addData(buffer, object2binary(pojo2game(object)));
			break;
		default:
			throw new IllegalArgumentException("Unrecognized type in GameObject serialization: " + typeId);
		}

		return buffer;
	}

	private GameDataWrapper binDecode_NULL(ByteBuf buffer) {
		return new GameDataWrapper(GameDataType.NULL, null);
	}

	private GameDataWrapper binDecode_BOOL(ByteBuf buffer) throws CodecException {
		byte boolByte = buffer.readByte();
		Boolean bool = null;

		if (boolByte == 0)
			bool = new Boolean(false);
		else if (boolByte == 1)
			bool = new Boolean(true);
		else {
			throw new CodecException("Error decoding Bool type. Illegal value: " + bool);
		}
		return new GameDataWrapper(GameDataType.BOOL, bool);
	}

	private GameDataWrapper binDecode_BYTE(ByteBuf buffer) {
		byte boolByte = buffer.readByte();

		return new GameDataWrapper(GameDataType.BYTE, Byte.valueOf(boolByte));
	}

	private GameDataWrapper binDecode_SHORT(ByteBuf buffer) {
		short shortValue = buffer.readShort();

		return new GameDataWrapper(GameDataType.SHORT, Short.valueOf(shortValue));
	}

	private GameDataWrapper binDecode_INT(ByteBuf buffer) {
		int intValue = buffer.readInt();

		return new GameDataWrapper(GameDataType.INT, Integer.valueOf(intValue));
	}

	private GameDataWrapper binDecode_LONG(ByteBuf buffer) {
		long longValue = buffer.readLong();

		return new GameDataWrapper(GameDataType.LONG, Long.valueOf(longValue));
	}

	private GameDataWrapper binDecode_FLOAT(ByteBuf buffer) {
		float floatValue = buffer.readFloat();

		return new GameDataWrapper(GameDataType.FLOAT, Float.valueOf(floatValue));
	}

	private GameDataWrapper binDecode_DOUBLE(ByteBuf buffer) {
		double doubleValue = buffer.readDouble();

		return new GameDataWrapper(GameDataType.DOUBLE, Double.valueOf(doubleValue));
	}

	private GameDataWrapper binDecode_UTF_STRING(ByteBuf buffer) throws CodecException {
		short strLen = buffer.readShort();

		if (strLen < 0) {
			throw new CodecException("Error decoding UtfString. Negative size: " + strLen);
		}

		byte[] strData = new byte[strLen];
		buffer.readBytes(strData, 0, strLen);

		String decodedString = new String(strData);
		return new GameDataWrapper(GameDataType.UTF_STRING, decodedString);
	}

	private GameDataWrapper binDecode_BOOL_ARRAY(ByteBuf buffer) throws CodecException {
		short arraySize = getTypeArraySize(buffer);
		List<Boolean> array = new ArrayList<Boolean>();

		for (int j = 0; j < arraySize; j++) {
			byte boolData = buffer.readByte();

			if (boolData == 0)
				array.add(Boolean.valueOf(false));
			else if (boolData == 1) {
				array.add(Boolean.valueOf(true));
			} else {
				throw new CodecException("Error decoding BoolArray. Invalid bool value: " + boolData);
			}
		}

		return new GameDataWrapper(GameDataType.BOOL_ARRAY, array);
	}

	private GameDataWrapper binDecode_BYTE_ARRAY(ByteBuf buffer) throws CodecException {
		int arraySize = buffer.readInt();

		if (arraySize < 0) {
			throw new CodecException("Error decoding typed array size. Negative size: " + arraySize);
		}
		byte[] byteData = new byte[arraySize];

		buffer.readBytes(byteData, 0, arraySize);

		return new GameDataWrapper(GameDataType.BYTE_ARRAY, byteData);
	}

	private GameDataWrapper binDecode_SHORT_ARRAY(ByteBuf buffer) throws CodecException {
		short arraySize = getTypeArraySize(buffer);
		List<Short> array = new ArrayList<Short>();

		for (int j = 0; j < arraySize; j++) {
			short shortValue = buffer.readShort();
			array.add(Short.valueOf(shortValue));
		}

		return new GameDataWrapper(GameDataType.SHORT_ARRAY, array);
	}

	private GameDataWrapper binDecode_INT_ARRAY(ByteBuf buffer) throws CodecException {
		short arraySize = getTypeArraySize(buffer);
		List<Integer> array = new ArrayList<Integer>();

		for (int j = 0; j < arraySize; j++) {
			int intValue = buffer.readInt();
			array.add(Integer.valueOf(intValue));
		}

		return new GameDataWrapper(GameDataType.INT_ARRAY, array);
	}

	private GameDataWrapper binDecode_LONG_ARRAY(ByteBuf buffer) throws CodecException {
		short arraySize = getTypeArraySize(buffer);
		List<Long> array = new ArrayList<Long>();

		for (int j = 0; j < arraySize; j++) {
			long longValue = buffer.readLong();
			array.add(Long.valueOf(longValue));
		}

		return new GameDataWrapper(GameDataType.LONG_ARRAY, array);
	}

	private GameDataWrapper binDecode_FLOAT_ARRAY(ByteBuf buffer) throws CodecException {
		short arraySize = getTypeArraySize(buffer);
		List<Float> array = new ArrayList<Float>();

		for (int j = 0; j < arraySize; j++) {
			float floatValue = buffer.readFloat();
			array.add(Float.valueOf(floatValue));
		}

		return new GameDataWrapper(GameDataType.FLOAT_ARRAY, array);
	}

	private GameDataWrapper binDecode_DOUBLE_ARRAY(ByteBuf buffer) throws CodecException {
		short arraySize = getTypeArraySize(buffer);
		List<Double> array = new ArrayList<Double>();

		for (int j = 0; j < arraySize; j++) {
			double doubleValue = buffer.readDouble();
			array.add(Double.valueOf(doubleValue));
		}

		return new GameDataWrapper(GameDataType.DOUBLE_ARRAY, array);
	}

	private GameDataWrapper binDecode_UTF_STRING_ARRAY(ByteBuf buffer) throws CodecException {
		short arraySize = getTypeArraySize(buffer);
		List<String> array = new ArrayList<String>();

		for (int j = 0; j < arraySize; j++) {
			short strLen = buffer.readShort();

			if (strLen < 0) {
				throw new CodecException("Error decoding UtfStringArray element. Element has negative size: " + strLen);
			}

			byte[] strData = new byte[strLen];
			buffer.readBytes(strData, 0, strLen);

			array.add(new String(strData));
		}

		return new GameDataWrapper(GameDataType.UTF_STRING_ARRAY, array);
	}

	private short getTypeArraySize(ByteBuf buffer) throws CodecException {
		short arraySize = buffer.readShort();

		if (arraySize < 0) {
			throw new CodecException("Error decoding typed array size. Negative size: " + arraySize);
		}
		return arraySize;
	}

	private ByteBuf binEncode_NULL(ByteBuf buffer) {
		return addData(buffer, new byte[1]);
	}

	private ByteBuf binEncode_BOOL(ByteBuf buffer, Boolean value) {
		byte[] data = new byte[2];
		data[0] = (byte) GameDataType.BOOL.getTypeID();
		data[1] = (value.booleanValue() ? (byte) 1 : (byte) 0);

		return addData(buffer, data);
	}

	private ByteBuf binEncode_BYTE(ByteBuf buffer, Byte value) {
		byte[] data = new byte[2];
		data[0] = (byte) GameDataType.BYTE.getTypeID();
		data[1] = value.byteValue();

		return addData(buffer, data);
	}

	private ByteBuf binEncode_SHORT(ByteBuf buffer, Short value) {
		ByteBuf buf = Unpooled.buffer(3);
		buf.writeByte((byte) GameDataType.SHORT.getTypeID());
		buf.writeShort(value.shortValue());

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_INT(ByteBuf buffer, Integer value) {
		ByteBuf buf = Unpooled.buffer(5);
		buf.writeByte((byte) GameDataType.INT.getTypeID());
		buf.writeInt(value.intValue());

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_LONG(ByteBuf buffer, Long value) {
		ByteBuf buf = Unpooled.buffer(9);
		buf.writeByte((byte) GameDataType.LONG.getTypeID());
		buf.writeLong(value.longValue());

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_FLOAT(ByteBuf buffer, Float value) {
		ByteBuf buf = Unpooled.buffer(5);
		buf.writeByte((byte) GameDataType.FLOAT.getTypeID());
		buf.writeFloat(value.floatValue());

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_DOUBLE(ByteBuf buffer, Double value) {
		ByteBuf buf = Unpooled.buffer(9);
		buf.writeByte((byte) GameDataType.DOUBLE.getTypeID());
		buf.writeDouble(value.doubleValue());

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_UTF_STRING(ByteBuf buffer, String value) {
		byte[] stringBytes = value.getBytes();
		ByteBuf buf = Unpooled.buffer(3 + stringBytes.length);
		buf.writeByte((byte) GameDataType.UTF_STRING.getTypeID());
		buf.writeShort((short) stringBytes.length);
		buf.writeBytes(stringBytes);

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_BOOL_ARRAY(ByteBuf buffer, Collection<Boolean> value) {
		ByteBuf buf = Unpooled.buffer(3 + value.size());
		buf.writeByte((byte) GameDataType.BOOL_ARRAY.getTypeID());
		buf.writeShort((short) value.size());

		for (Iterator<Boolean> localIterator = value.iterator(); localIterator.hasNext();) {
			boolean b = localIterator.next().booleanValue();

			buf.writeByte(b ? (byte) 1 : (byte) 0);
		}

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_BYTE_ARRAY(ByteBuf buffer, byte[] value) {
		ByteBuf buf = Unpooled.buffer(5 + value.length);
		buf.writeByte((byte) GameDataType.BYTE_ARRAY.getTypeID());
		buf.writeInt(value.length);

		buf.writeBytes(value);

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_SHORT_ARRAY(ByteBuf buffer, Collection<Short> value) {
		ByteBuf buf = Unpooled.buffer(3 + 2 * value.size());
		buf.writeByte((byte) GameDataType.SHORT_ARRAY.getTypeID());
		buf.writeShort((short) value.size());

		for (Iterator<Short> localIterator = value.iterator(); localIterator.hasNext();) {
			short item = localIterator.next().shortValue();

			buf.writeShort(item);
		}

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_INT_ARRAY(ByteBuf buffer, Collection<Integer> value) {
		ByteBuf buf = Unpooled.buffer(3 + 4 * value.size());
		buf.writeByte((byte) GameDataType.INT_ARRAY.getTypeID());
		buf.writeShort((short) value.size());

		for (Iterator<Integer> localIterator = value.iterator(); localIterator.hasNext();) {
			int item = localIterator.next().intValue();

			buf.writeInt(item);
		}

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_LONG_ARRAY(ByteBuf buffer, Collection<Long> value) {
		ByteBuf buf = Unpooled.buffer(3 + 8 * value.size());
		buf.writeByte((byte) GameDataType.LONG_ARRAY.getTypeID());
		buf.writeShort((short) value.size());

		for (Iterator<Long> localIterator = value.iterator(); localIterator.hasNext();) {
			long item = localIterator.next().longValue();

			buf.writeLong(item);
		}

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_FLOAT_ARRAY(ByteBuf buffer, Collection<Float> value) {
		ByteBuf buf = Unpooled.buffer(3 + 4 * value.size());
		buf.writeByte((byte) GameDataType.FLOAT_ARRAY.getTypeID());
		buf.writeShort((short) value.size());

		for (Iterator<Float> localIterator = value.iterator(); localIterator.hasNext();) {
			float item = localIterator.next().floatValue();

			buf.writeFloat(item);
		}

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_DOUBLE_ARRAY(ByteBuf buffer, Collection<Double> value) {
		ByteBuf buf = Unpooled.buffer(3 + 8 * value.size());
		buf.writeByte((byte) GameDataType.DOUBLE_ARRAY.getTypeID());
		buf.writeShort((short) value.size());

		for (Iterator<Double> localIterator = value.iterator(); localIterator.hasNext();) {
			double item = localIterator.next().doubleValue();

			buf.writeDouble(item);
		}

		return addData(buffer, buf.array());
	}

	private ByteBuf binEncode_UTF_STRING_ARRAY(ByteBuf buffer, Collection<String> value) {
		int stringDataLen = 0;
		byte binStrings[][] = new byte[value.size()][];
		int count = 0;
		for (Iterator<String> iterator = value.iterator(); iterator.hasNext();) {
			String item = iterator.next();
			byte binStr[] = item.getBytes();
			binStrings[count++] = binStr;
			stringDataLen += 2 + binStr.length;
		}

		ByteBuf buf = Unpooled.buffer(3 + stringDataLen);
		buf.writeByte((byte) GameDataType.UTF_STRING_ARRAY.getTypeID());
		buf.writeShort((short) value.size());
		byte abyte0[][];
		int j = (abyte0 = binStrings).length;
		for (int i = 0; i < j; i++) {
			byte binItem[] = abyte0[i];
			buf.writeShort((short) binItem.length);
			buf.writeBytes(binItem);
		}

		return addData(buffer, buf.array());
	}

	private ByteBuf encodeGameObjectKey(ByteBuf buffer, String value) {
		ByteBuf buf = Unpooled.buffer(2 + value.length());
		buf.writeShort((short) value.length());
		buf.writeBytes(value.getBytes());

		return addData(buffer, buf.array());
	}

	private ByteBuf addData(ByteBuf buffer, byte[] newData) {
		// netty的byteBuf有自动扩容功能，不用担心容量
		buffer.writeBytes(newData);

		return buffer;
	}

	public IGameObject pojo2game(Object pojo) {
		IGameObject gameObj = GameObject.newInstance();
		try {
			convertPojo(pojo, gameObj);
		} catch (Exception e) {
			throw new GameRuntimeException(e);
		}

		return gameObj;
	}

	private void convertPojo(Object pojo, IGameObject gameObj) throws Exception {
		Class<? extends Object> pojoClazz = pojo.getClass();
		String classFullName = pojoClazz.getCanonicalName();

		if (classFullName == null) {
			throw new IllegalArgumentException("Anonymous classes cannot be serialized!");
		}
		if (!(pojo instanceof SerializableGameType)) {
			throw new IllegalStateException("Cannot serialize object: " + pojo + ", type: " + classFullName
					+ " -- It doesn't implement the SerializableGameType interface");
		}
		IGameArray fieldList = GameArray.newInstance();

		gameObj.putUtfString(CLASS_MARKER_KEY, classFullName);
		gameObj.putGameArray(CLASS_FIELDS_KEY, fieldList);

		for (Field field : pojoClazz.getDeclaredFields()) {
			try {
				int modifiers = field.getModifiers();

				if ((Modifier.isTransient(modifiers)) || (Modifier.isStatic(modifiers))) {
					continue;
				}
				String fieldName = field.getName();
				Object fieldValue = null;

				if (Modifier.isPublic(modifiers)) {
					fieldValue = field.get(pojo);
				} else {
					fieldValue = readValueFromGetter(fieldName, field.getType().getSimpleName(), pojo);
				}

				IGameObject fieldDescriptor = GameObject.newInstance();

				fieldDescriptor.putUtfString(FIELD_NAME_KEY, fieldName);

				fieldDescriptor.put(FIELD_VALUE_KEY, wrapPojoField(fieldValue));

				fieldList.addGameObject(fieldDescriptor);
			} catch (NoSuchMethodException err) {
				logger.info("-- No public getter -- Serializer skippingprivate field: " + field.getName()
						+ ", from class: " + pojoClazz);
				err.printStackTrace();
			}
		}
	}

	private Object readValueFromGetter(String fieldName, String type, Object pojo) throws Exception {
		Object value = null;
		// boolean isBool = type.equalsIgnoreCase("boolean");

		String getterName = "get" + StringUtils.capitalize(fieldName);

		Method getterMethod = pojo.getClass().getMethod(getterName, new Class[0]);
		value = getterMethod.invoke(pojo, new Object[0]);

		return value;
	}

	private GameDataWrapper wrapPojoField(Object value) {
		if (value == null) {
			return new GameDataWrapper(GameDataType.NULL, null);
		}

		GameDataWrapper wrapper = null;

		if ((value instanceof Boolean)) {
			wrapper = new GameDataWrapper(GameDataType.BOOL, value);
		} else if ((value instanceof Byte)) {
			wrapper = new GameDataWrapper(GameDataType.BYTE, value);
		} else if ((value instanceof Short)) {
			wrapper = new GameDataWrapper(GameDataType.SHORT, value);
		} else if ((value instanceof Integer)) {
			wrapper = new GameDataWrapper(GameDataType.INT, value);
		} else if ((value instanceof Long)) {
			wrapper = new GameDataWrapper(GameDataType.LONG, value);
		} else if ((value instanceof Float)) {
			wrapper = new GameDataWrapper(GameDataType.FLOAT, value);
		} else if ((value instanceof Double)) {
			wrapper = new GameDataWrapper(GameDataType.DOUBLE, value);
		} else if ((value instanceof String)) {
			wrapper = new GameDataWrapper(GameDataType.UTF_STRING, value);
		} else if (value.getClass().isArray()) {
			wrapper = new GameDataWrapper(GameDataType.Game_ARRAY, unrollArray((Object[]) value));
		} else if ((value instanceof Collection)) {
			wrapper = new GameDataWrapper(GameDataType.Game_ARRAY, unrollCollection((Collection<?>) value));
		} else if ((value instanceof Map)) {
			wrapper = new GameDataWrapper(GameDataType.Game_OBJECT, unrollMap((Map<?, ?>) value));
		} else if ((value instanceof SerializableGameType)) {
			wrapper = new GameDataWrapper(GameDataType.Game_OBJECT, pojo2game(value));
		}

		return wrapper;
	}

	private IGameArray unrollArray(Object[] arr) {
		IGameArray array = GameArray.newInstance();

		for (Object item : arr) {
			array.add(wrapPojoField(item));
		}

		return array;
	}

	private IGameArray unrollCollection(Collection<?> collection) {
		IGameArray array = GameArray.newInstance();

		for (Iterator<?> localIterator = collection.iterator(); localIterator.hasNext();) {
			Object item = localIterator.next();

			array.add(wrapPojoField(item));
		}

		return array;
	}

	private IGameObject unrollMap(Map<?, ?> map) {
		IGameObject gameObj = GameObject.newInstance();
		Set<?> entries = map.entrySet();

		for (Iterator<?> iter = entries.iterator(); iter.hasNext();) {
			Map.Entry<?, ?> item = (Map.Entry<?, ?>) iter.next();
			Object key = item.getKey();

			if (!(key instanceof String))
				continue;
			gameObj.put((String) key, wrapPojoField(item.getValue()));
		}

		return gameObj;
	}

	public Object game2pojo(IGameObject gameObj) {
		Object pojo = null;

		if ((!gameObj.containsKey(CLASS_MARKER_KEY)) && (!gameObj.containsKey(CLASS_FIELDS_KEY))) {
			throw new GameRuntimeException("The GameObject passed does not represent any serialized class.");
		}
		try {
			String className = gameObj.getUtfString(CLASS_MARKER_KEY);
			Class<?> theClass = Class.forName(className);
			pojo = theClass.newInstance();

			if (!(pojo instanceof SerializableGameType)) {
				throw new IllegalStateException("Cannot deserialize object: " + pojo + ", type: " + className
						+ " -- It doesn't implement the SerializableGameType interface");
			}
			convertGameObject(gameObj.getGameArray(CLASS_FIELDS_KEY), pojo);
		} catch (Exception e) {
			throw new GameRuntimeException(e);
		}

		return pojo;
	}

	private void convertGameObject(IGameArray fieldList, Object pojo) throws Exception {
		for (int j = 0; j < fieldList.size(); j++) {
			IGameObject fieldDescriptor = fieldList.getGameObject(j);
			String fieldName = fieldDescriptor.getUtfString(FIELD_NAME_KEY);
			Object fieldValue = unwrapPojoField(fieldDescriptor.get(FIELD_VALUE_KEY));

			setObjectField(pojo, fieldName, fieldValue);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setObjectField(Object pojo, String fieldName, Object fieldValue) throws Exception {
		Class<? extends Object> pojoClass = pojo.getClass();
		Field field = pojoClass.getDeclaredField(fieldName);
		int fieldModifier = field.getModifiers();

		if (Modifier.isTransient(fieldModifier)) {
			return;
		}

		boolean isArray = field.getType().isArray();
		if (isArray) {
			if (!(fieldValue instanceof Collection)) {
				throw new GameRuntimeException(
						"Problem during GameObject => POJO conversion. Found array field in POJO: " + fieldName
								+ ", but data is not a Collection!");
			}

			Collection<?> collection = (Collection<?>) fieldValue;
			fieldValue = collection.toArray();
			int arraySize = collection.size();

			Object typedArray = Array.newInstance(field.getType().getComponentType(), arraySize);
			System.arraycopy(fieldValue, 0, typedArray, 0, arraySize);

			fieldValue = typedArray;
		} else if ((fieldValue instanceof Collection)) {
			collection = (Collection<?>) fieldValue;
			String fieldClass = field.getType().getSimpleName();

			if ((fieldClass.equals("ArrayList")) || (fieldClass.equals("List"))) {
				fieldValue = new ArrayList<Object>(collection);
			}
			if (fieldClass.equals("CopyOnWriteArrayList")) {
				fieldValue = new CopyOnWriteArrayList<Object>(collection);
			} else if (fieldClass.equals("LinkedList")) {
				fieldValue = new LinkedList<Object>(collection);
			} else if (fieldClass.equals("Vector")) {
				fieldValue = new Vector<Object>(collection);
			} else if ((fieldClass.equals("Set")) || (fieldClass.equals("HashSet"))) {
				fieldValue = new HashSet<Object>(collection);
			} else if (fieldClass.equals("LinkedHashSet")) {
				fieldValue = new LinkedHashSet<Object>(collection);
			} else if (fieldClass.equals("TreeSet")) {
				fieldValue = new TreeSet<Object>(collection);
			} else if (fieldClass.equals("CopyOnWriteArraySet")) {
				fieldValue = new CopyOnWriteArraySet<Object>(collection);
			} else if ((fieldClass.equals("Queue")) || (fieldClass.equals("PriorityQueue"))) {
				fieldValue = new PriorityQueue<Object>(collection);
			} else if ((fieldClass.equals("BlockingQueue")) || (fieldClass.equals("LinkedBlockingQueue"))) {
				fieldValue = new LinkedBlockingQueue<Object>(collection);
			} else if (fieldClass.equals("PriorityBlockingQueue")) {
				fieldValue = new PriorityBlockingQueue<Object>(collection);
			} else if (fieldClass.equals("ConcurrentLinkedQueue")) {
				fieldValue = new ConcurrentLinkedQueue<Object>(collection);
			} else if (fieldClass.equals("DelayQueue")) {
				fieldValue = new DelayQueue(collection);
			} else if ((fieldClass.equals("Deque")) || (fieldClass.equals("ArrayDeque"))) {
				fieldValue = new ArrayDeque<Object>(collection);
			} else if (fieldClass.equals("LinkedBlockingDeque")) {
				fieldValue = new LinkedBlockingDeque<Object>(collection);
			}

		}

		if (Modifier.isPublic(fieldModifier))
			field.set(pojo, fieldValue);
		else
			writeValueFromSetter(field, pojo, fieldValue);
	}

	private void writeValueFromSetter(Field field, Object pojo, Object fieldValue) throws Exception {
		String setterName = "set" + StringUtils.capitalize(field.getName());
		try {
			Method setterMethod = pojo.getClass().getMethod(setterName, new Class[] { field.getType() });
			setterMethod.invoke(pojo, new Object[] { fieldValue });
		} catch (NoSuchMethodException e) {
			this.logger.info("-- No public setter -- Serializer skipping private field: " + field.getName()
					+ ", from class: " + pojo.getClass().getName());
		}
	}

	private Object unwrapPojoField(GameDataWrapper wrapper) {
		Object obj = null;

		GameDataType type = wrapper.getTypeId();

		if (type.getTypeID() <= GameDataType.UTF_STRING.getTypeID()) {
			obj = wrapper.getObject();
		} else if (type == GameDataType.Game_ARRAY) {
			obj = rebuildArray((IGameArray) wrapper.getObject());
		} else if (type == GameDataType.Game_OBJECT) {
			obj = rebuildMap((IGameObject) wrapper.getObject());
		} else if (type == GameDataType.CLASS) {
			obj = wrapper.getObject();
		}

		return obj;
	}

	private Object rebuildArray(IGameArray gameArray) {
		Collection<Object> collection = new ArrayList<Object>();

		for (Iterator<?> iter = gameArray.iterator(); iter.hasNext();) {
			Object item = unwrapPojoField((GameDataWrapper) iter.next());
			collection.add(item);
		}

		return collection;
	}

	private Object rebuildMap(IGameObject gameObj) {
		Map<String, Object> map = new HashMap<String, Object>();

		for (String key : gameObj.getKeys()) {
			GameDataWrapper wrapper = gameObj.get(key);
			map.put(key, unwrapPojoField(wrapper));
		}

		return map;
	}

}