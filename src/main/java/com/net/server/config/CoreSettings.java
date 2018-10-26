package com.net.server.config;

public final class CoreSettings {
	public String readBufferType = "HEAP";
	public String writeBufferType = "HEAP";
	public int maxIncomingRequestSize = 4096;
	public int socketAcceptorThreadPoolSize = 1;
	public int socketReaderThreadPoolSize = 1;
	public int socketWriterThreadPoolSize = 1;
	public int sessionPacketQueueSize = 120;
	public boolean tcpNoDelay = false;
	public boolean packetDebug = false;
	public boolean lagDebug = false;
	public int bbMaxLogFiles = 10;
	public int bbMaxLogFileSize = 1000000;
	public boolean bbDebugMode = false;
}