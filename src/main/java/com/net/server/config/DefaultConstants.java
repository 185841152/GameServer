package com.net.server.config;

public final class DefaultConstants {
	public static final String LOG_LINE_SEPARATOR = "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::";
	public static final String CONFIG_FOLDER = "config/";
	public static final String CORE_CFG_FILE = "config/core.xml";
	public static final String SERVER_CFG_FILE = "config/server.xml";
	public static final String ZONES_FOLDER = "zones/";
	public static final String ZONE_FILE_EXTENSION = ".zone.xml";
	public static final String ZONE_DEFAULT_GROUP = "default";
	public static final String DATE_FORMAT = "dd MMM yyyy";
	public static final String TIME_FORMAT = "HH:mm:ss,SSS";
	public static final Byte CORE_SYSTEM_CONTROLLER_ID = new Byte("0");
	public static final Byte CORE_EXTENSIONS_CONTROLLER_ID = new Byte("1");
	public static final Byte CORE_SMASHER_CONTROLLER_ID = new Byte("2");
	public static final int CORE_MAX_INCOMING_REQUEST_SIZE = 4096;
	public static final int CORE_SESSION_QUEUE_SIZE = 120;
	public static final int CORE_SOCKET_ACCEPTOR_THREAD_POOL_SIZE = 1;
	public static final int CORE_SOCKET_READER_THREAD_POOL_SIZE = 1;
	public static final int CORE_SOCKET_WRITER_THREAD_POOL_SIZE = 1;
	public static final int CORE_MAX_CONNECTIONS_FROM_SAME_IP = 3;
}