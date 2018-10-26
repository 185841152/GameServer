package com.net.server.api;

public enum GenericMessageType {
	PUBLIC_MSG(0), PRIVATE_MSG(1), MODERATOR_MSG(2), ADMING_MSG(3), OBJECT_MSG(4), BUDDY_MSG(5);

	private int id;

	private GenericMessageType(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public static GenericMessageType fromId(int id) {
		GenericMessageType type = null;

		for (GenericMessageType item : values()) {
			if (item.getId() != id)
				continue;
			type = item;
			break;
		}

		return type;
	}
}