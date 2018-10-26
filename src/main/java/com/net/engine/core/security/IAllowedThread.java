package com.net.engine.core.security;

public abstract interface IAllowedThread {
	public abstract String getName();

	public abstract ThreadComparisonType getComparisonType();
}