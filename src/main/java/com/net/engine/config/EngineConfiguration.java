package com.net.engine.config;

import java.util.ArrayList;
import java.util.List;

import com.net.engine.websocket.WebSocketConfig;
import com.net.server.config.DefaultConstants;
import com.net.server.config.ServerSettings.ClusterAddress;

public final class EngineConfiguration {
	private String localNodeName = "local";

	private int defaultMaxSessionIdleTime = 60;
	private int defaultMaxLoggedInSessionIdleTime = 120;

	private int connectionCleanerInterval = 60;
	private int acceptorThreadPoolSize = 1;
	private int readerThreadPoolSize = 1;
	private int writerThreadPoolSize = 1;

	private int sessionPacketQueueMaxSize = 200;

	private boolean clustered = false;
	private boolean nagleAlgorithm = true;

	private String sessionManagerClass = "com.smartfoxserver.bitswarm.sessions.DefaultSessionManager";
	private String ioHandlerClass = "com.smartfoxserver.bitswarm.io.protocols.text.TextIOHandler";
	private String packetQueuePolicyClass = "com.net.engine.sessions.DefaultPacketQueuePolicy";

	private int readMaxBufferSize = 8192;
	private int writeMaxBufferSize = 32768;
	private int maxIncomingRequestSize = DefaultConstants.CORE_MAX_INCOMING_REQUEST_SIZE;
	private List<SocketConfig> bindableAddresses;
	private List<ClusterAddress> clusterAddresses;
	private List<ControllerConfig> controllerConfigs;
	private String clusterUniqueIdGeneratorClass;
	private int globalReconnectionSeconds = 0;
	private int maxConnectionsFromSameIp = 3;

	private boolean packetDebug = false;

	private boolean lagDebug = false;

	private WebSocketConfig webSocketEngineConfig = new WebSocketConfig();

	public EngineConfiguration() {
		this.bindableAddresses = new ArrayList<SocketConfig>();
		this.clusterAddresses = new ArrayList<ClusterAddress>();
		this.controllerConfigs = new ArrayList<ControllerConfig>();
	}

	public void addController(ControllerConfig cfg) {
		this.controllerConfigs.add(cfg);
	}

	public List<ControllerConfig> getControllerConfigs() {
		return this.controllerConfigs;
	}

	public void addBindableAddress(SocketConfig socket) {
		this.bindableAddresses.add(socket);
	}
	
	public void addClusterAddress(ClusterAddress address){
		this.clusterAddresses.add(address);
	}

	public List<SocketConfig> getBindableSockets() {
		return this.bindableAddresses;
	}
	
	public List<ClusterAddress> getClusterAddress(){
		return this.clusterAddresses;
	}

	public String getSessionManagerClass() {
		return this.sessionManagerClass;
	}

	public void setSessionManagerClass(String sessionManagerClass) {
		this.sessionManagerClass = sessionManagerClass;
	}

	public boolean isNagleAlgorithm() {
		return this.nagleAlgorithm;
	}

	public void setNagleAlgorithm(boolean nagleAlgorithm) {
		this.nagleAlgorithm = nagleAlgorithm;
	}

	public String getLocalNodeName() {
		return this.localNodeName;
	}

	public void setLocalNodeName(String localNodeName) {
		this.localNodeName = localNodeName;
	}

	public boolean isClustered() {
		return this.clustered;
	}

	public int getDefaultMaxSessionIdleTime() {
		return this.defaultMaxSessionIdleTime;
	}

	public void setDefaultMaxSessionIdleTime(int defaultMaxSessionIdleTime) {
		this.defaultMaxSessionIdleTime = defaultMaxSessionIdleTime;
	}

	public int getDefaultMaxLoggedInSessionIdleTime() {
		return this.defaultMaxLoggedInSessionIdleTime;
	}

	public void setDefaultMaxLoggedInSessionIdleTime(int defaultMaxLoggedInSessionIdleTime)
			throws IllegalArgumentException {
		if (defaultMaxLoggedInSessionIdleTime < this.defaultMaxSessionIdleTime) {
			String errorMsg = String.format("userMaxIdleTime (%s) cannot be smaller than sessionMaxIdleTime (%s)",
					new Object[] { Integer.valueOf(defaultMaxLoggedInSessionIdleTime),
							Integer.valueOf(this.defaultMaxSessionIdleTime) });

			throw new IllegalArgumentException(errorMsg);
		}

		this.defaultMaxLoggedInSessionIdleTime = defaultMaxLoggedInSessionIdleTime;
	}

	public int getConnectionCleanerInterval() {
		return this.connectionCleanerInterval;
	}

	public void setConnectionCleanerInterval(int connectionCleanerInterval) {
		this.connectionCleanerInterval = connectionCleanerInterval;
	}

	public int getAcceptorThreadPoolSize() {
		return this.acceptorThreadPoolSize;
	}

	public void setAcceptorThreadPoolSize(int acceptorThreadPoolSize) {
		this.acceptorThreadPoolSize = acceptorThreadPoolSize;
	}

	public int getReaderThreadPoolSize() {
		return this.readerThreadPoolSize;
	}

	public void setReaderThreadPoolSize(int readerThreadPoolSize) {
		this.readerThreadPoolSize = readerThreadPoolSize;
	}

	public int getWriterThreadPoolSize() {
		return this.writerThreadPoolSize;
	}

	public void setWriterThreadPoolSize(int writerThreadPoolSize) {
		this.writerThreadPoolSize = writerThreadPoolSize;
	}

	public String getIoHandlerClass() {
		return this.ioHandlerClass;
	}

	public void setIoHandlerClass(String ioHandlerClass) {
		this.ioHandlerClass = ioHandlerClass;
	}

	public int getReadMaxBufferSize() {
		return this.readMaxBufferSize;
	}

	public void setReadMaxBufferSize(int readMaxBufferSize) {
		this.readMaxBufferSize = readMaxBufferSize;
	}

	public void setClustered(boolean clustered) {
		this.clustered = clustered;
	}

	public List<SocketConfig> getBindableAddresses() {
		return this.bindableAddresses;
	}

	public void setBindableAddresses(List<SocketConfig> bindableAddresses) {
		this.bindableAddresses = bindableAddresses;
	}

	public int getSessionPacketQueueMaxSize() {
		return this.sessionPacketQueueMaxSize;
	}

	public void setSessionPacketQueueMaxSize(int sessionPacketQueueMaxSize) {
		this.sessionPacketQueueMaxSize = sessionPacketQueueMaxSize;
	}

	public String getPacketQueuePolicyClass() {
		return this.packetQueuePolicyClass;
	}

	public void setPacketQueuePolicyClass(String packetQueuePolicyClass) {
		this.packetQueuePolicyClass = packetQueuePolicyClass;
	}

	public int getWriteMaxBufferSize() {
		return this.writeMaxBufferSize;
	}

	public void setWriteMaxBufferSize(int writeMaxBufferSize) {
		this.writeMaxBufferSize = writeMaxBufferSize;
	}

	public String getClusterUniqueIdGeneratorClass() {
		return this.clusterUniqueIdGeneratorClass;
	}

	public void setClusterUniqueIdGeneratorClass(String clusterUniqueIdGeneratorClass) {
		this.clusterUniqueIdGeneratorClass = clusterUniqueIdGeneratorClass;
	}

	public int getMaxIncomingRequestSize() {
		return this.maxIncomingRequestSize;
	}

	public void setMaxIncomingRequestSize(int maxIncomingRequestSize) {
		this.maxIncomingRequestSize = maxIncomingRequestSize;
	}

	public int getGlobalReconnectionSeconds() {
		return this.globalReconnectionSeconds;
	}

	public void setGlobalReconnectionSeconds(int globalReconnectionSeconds) {
		this.globalReconnectionSeconds = globalReconnectionSeconds;
	}

	public int getMaxConnectionsFromSameIp() {
		return this.maxConnectionsFromSameIp;
	}

	public void setMaxConnectionsFromSameIp(int maxConnectionsFromSameIp) {
		this.maxConnectionsFromSameIp = maxConnectionsFromSameIp;
	}

	public void setPacketDebug(boolean packetDebug) {
		this.packetDebug = packetDebug;
	}

	public boolean isPacketDebug() {
		return this.packetDebug;
	}

	public void setLagDebug(boolean lagDebug) {
		this.lagDebug = lagDebug;
	}

	public boolean isLagDebug() {
		return this.lagDebug;
	}

	public WebSocketConfig getWebSocketEngineConfig() {
		return this.webSocketEngineConfig;
	}

	public void setWebSocketEngineConfig(WebSocketConfig webSocketEngineConfig) {
		this.webSocketEngineConfig = webSocketEngineConfig;
	}
}