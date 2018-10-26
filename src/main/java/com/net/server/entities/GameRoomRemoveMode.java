package com.net.server.entities;

public enum GameRoomRemoveMode {
	DEFAULT,

	WHEN_EMPTY,

	WHEN_EMPTY_AND_CREATOR_IS_GONE,

	NEVER_REMOVE;

	public static GameRoomRemoveMode fromString(String id) {
		return valueOf(id.toUpperCase());
	}
}