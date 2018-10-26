package com.net.server.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.net.server.data.IGameObject;
import com.net.server.data.GameObject;
import com.net.server.util.executor.ExecutorConfig;

public class ServerSettings implements Serializable {
	private static final long serialVersionUID = 2784901283989933331L;
	
	
	public volatile List<SocketAddress> socketAddresses = new ArrayList<SocketAddress>();
	public volatile List<ClusterAddress> clusterAddresses=new ArrayList<ClusterAddress>();
	
	public volatile boolean isCluster=false;

	public volatile IpFilterSettings ipFilter = new IpFilterSettings();

	public volatile int systemControllerRequestQueueSize = 10000;
	public volatile int extensionControllerRequestQueueSize = 10000;
	public volatile int schedulerThreadPoolSize = 1;
	public volatile int protocolCompressionThreshold = 300;
	public String protocolMode;
	public boolean useBinaryProtocol = true;

	public BannedUserManagerSettings bannedUserManager = new BannedUserManagerSettings();

	public WebSocketEngineSettings webSocket = new WebSocketEngineSettings();

	public int sessionMaxIdleTime;
	public int userMaxIdleTime;

	public volatile boolean ghostHunterEnabled = true;

	public volatile boolean statsExtraLoggingEnabled = true;

	public volatile boolean enableSmasherController = true;

	public ExecutorConfig systemThreadPoolSettings = new ExecutorConfig();
	public ExecutorConfig extensionThreadPoolSettings = new ExecutorConfig();

	public IGameObject toSFSObject() {
		IGameObject sfsObj = GameObject.newInstance();

		return sfsObj;
	}

	public static ServerSettings fromSFSObject(IGameObject sfsObj) {
		ServerSettings settings = new ServerSettings();

		return settings;
	}


	public static final class BannedUserManagerSettings implements Serializable {
		private static final long serialVersionUID = 7325819287960605109L;
		public boolean isAutoRemove = true;
		public boolean isPersistent = true;
		public String customPersistenceClass = null;
	}

	public static final class IpFilterSettings implements Serializable {
		private static final long serialVersionUID = -5489544308506908885L;
		public List<String> addressBlackList = new ArrayList<String>();
		public List<String> addressWhiteList = new ArrayList<String>();
		public volatile int maxConnectionsPerAddress = 5;
	}


	public static final class SocketAddress implements Serializable {
		private static final long serialVersionUID = -7688258623407767538L;
		public static final String TYPE_UDP = "UDP";
		public static final String TYPE_TCP = "TCP";
		public volatile String address = "127.0.0.1";
		public volatile int port = 9339;
		public volatile String type = "TCP";
	}

	public static final class ClusterAddress implements Serializable {
		private static final long serialVersionUID = -7688258623407767538L;
		public volatile String address = "127.0.0.1";
		public volatile int port = 9339;
	}

	public static final class WebSocketEngineSettings implements Serializable {
		private static final long serialVersionUID = 6733589672382247974L;
		public boolean isActive = false;
		public String bindAddress = "127.0.0.1";
		public int tcpPort = 8888;
		public int sslPort = 8843;
		public List<String> validDomains = new ArrayList<String>();
		public boolean isSSL = false;
		public String keyStoreFile = DefaultConstants.CONFIG_FOLDER+"keystore.jks";
		public String keyStorePassword = "password";
	}
}