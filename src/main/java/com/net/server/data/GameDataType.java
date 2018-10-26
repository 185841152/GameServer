package com.net.server.data;

public enum GameDataType {
	NULL(0), BOOL(1), BYTE(2), SHORT(3), INT(4), LONG(5), FLOAT(6), DOUBLE(7), UTF_STRING(8), BOOL_ARRAY(9), BYTE_ARRAY(10), 
	SHORT_ARRAY(11), INT_ARRAY(12), LONG_ARRAY(13), FLOAT_ARRAY(14), DOUBLE_ARRAY(15), UTF_STRING_ARRAY(16), Game_ARRAY(17), 
	Game_OBJECT(18), CLASS(19);

	private int typeID;

	private GameDataType(int typeID) {
		this.typeID = typeID;
	}

	public static GameDataType fromTypeId(int typeId) {
		for (GameDataType item : values()) {
			if (item.getTypeID() == typeId) {
				return item;
			}
		}

		throw new IllegalArgumentException("Unknown typeId for GameDataType");
	}

	public static GameDataType fromClass(@SuppressWarnings("rawtypes") Class clazz) {
		return null;
	}

	public int getTypeID() {
		return this.typeID;
	}
}