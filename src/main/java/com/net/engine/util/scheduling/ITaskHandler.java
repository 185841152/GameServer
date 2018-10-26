package com.net.engine.util.scheduling;

public abstract interface ITaskHandler {
	public abstract void doTask(Task paramTask) throws Exception;
}