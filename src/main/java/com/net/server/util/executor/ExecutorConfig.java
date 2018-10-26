package com.net.server.util.executor;

import java.io.Serializable;

public class ExecutorConfig implements Serializable {
	private static final long serialVersionUID = 503536543992946473L;
	public String name = null;

	public int coreThreads = 16;

	public int backupThreads = 8;

	public int maxBackups = 2;

	public int queueSizeTriggeringBackup = 500;

	public int secondsTriggeringBackup = 60;

	public int backupThreadsExpiry = 3600;

	public int queueSizeTriggeringBackupExpiry = 300;

	public boolean logActivity = true;

	public int queueFullWarningInterval = 300;
}