package com.net.engine.core.security;

public final class EngineThread implements IAllowedThread {
	private String name;
	private ThreadComparisonType comparisonType;

	public EngineThread(String name, ThreadComparisonType comparisonType) {
		this.name = name;
		this.comparisonType = comparisonType;
	}

	public String getName() {
		return this.name;
	}

	public ThreadComparisonType getComparisonType() {
		return this.comparisonType;
	}
}