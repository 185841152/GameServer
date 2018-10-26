package com.net.server.entities.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.service.BaseCoreService;
import com.net.server.GameServer;
import com.net.server.api.CreateRoomSettings;
import com.net.server.config.IConfigurator;
import com.net.server.config.ZoneSettings;
import com.net.server.config.ZoneSettings.RoomSettings;
import com.net.server.entities.GameRoomRemoveMode;
import com.net.server.entities.GameZone;
import com.net.server.entities.Room;
import com.net.server.entities.Zone;
import com.net.server.exceptions.GameException;
import com.net.server.mmo.CreateMMORoomSettings;
import com.net.server.mmo.MMOHelper;
import com.net.server.mmo.Vec3D;

public class GameZoneManager extends BaseCoreService implements IZoneManager {
	private Logger logger=LoggerFactory.getLogger(GameZoneManager.class);
	private IConfigurator configurator;
	private boolean inited = false;
	private GameServer server;
	private ConcurrentMap<String, Zone> zones;

	public GameZoneManager() {
		if (this.zones == null) {
			this.zones = new ConcurrentHashMap<String, Zone>();
		}
	}

	public synchronized void init(Object o) {
		if (!this.inited) {
			super.init(o);
			this.server = GameServer.getInstance();
			this.configurator = this.server.getConfigurator();
			this.inited = true;
		}
	}

	public List<Zone> getZoneList() {
		return new ArrayList<Zone>(zones.values());
	}

	public Zone getZoneByName(String paramString) {
		return zones.get(paramString);
	}

	public Zone getZoneById(int paramInt) {
		return null;
	}

	public void initializeZones() throws GameException {
		List<ZoneSettings> zoneSettings = this.configurator.loadZonesConfiguration();
		for (ZoneSettings settings : zoneSettings) {
			addZone(createZone(settings));
		}
	}

	public void addZone(Zone zone) {
		this.zones.put(zone.getName(), zone);
	}
	
	public Zone createZone(ZoneSettings zoneSettings) throws GameException {
		Zone zone = new GameZone(zoneSettings.getName(), zoneSettings.getId());
		zone.setUserCountChangeUpdateInterval(zoneSettings.getUserCountChangeUpdateInterval());
		int theZoneIdleTime = this.server.getConfigurator().getServerSettings().userMaxIdleTime;
		if (zoneSettings.overrideMaxUserIdleTime > 0) {
			if (zoneSettings.overrideMaxUserIdleTime >= this.server.getConfigurator().getServerSettings().sessionMaxIdleTime) {
				theZoneIdleTime = zoneSettings.overrideMaxUserIdleTime;
			} else {
				this.logger.warn(String.format(
						"%s - Could not override maxUserIdleTime. The provided value (%s sec) is < sessionMaxIdleTime (%s sec). You must provide a value > sessionMaxIdleTime. Please double check your configuration.",
						new Object[] { zone, Integer.valueOf(zoneSettings.overrideMaxUserIdleTime),
								Integer.valueOf(this.server.getConfigurator().getServerSettings().sessionMaxIdleTime) }));
			}
		}
		zone.setMaxUserIdleTime(theZoneIdleTime);
		zone.setUserReconnectionSeconds(zoneSettings.userReconnectionSeconds);
		//写入服务器列表
		//服务器信息写入集群SERVER MAP
//		String nodeId = ClusterUtils.generateNodeId();
//		int serverId=zoneSettings.getId();
//		Map<String, String> serverMap=new HashMap<String, String>();
//		serverMap.put("cu", "0");
//		serverMap.put("mu", zoneSettings.getMaxUsers()+"");
//		serverMap.put("n", nodeId);
//		serverMap.put("sn", zoneSettings.getServerName());
//		serverMap.put("i", serverId+"");
//		serverMap.put("s", zoneSettings.getStatus()+"");
//		serverMap.put("z", zoneSettings.getName());
//		RedisOperate jedis=BeanManager.getInstance().getRedisOperate();
//		jedis.setMap(serverId+":"+nodeId+":"+zoneSettings.getName(), serverMap, 0);
//		jedis.setSetAdd("servers", serverId+":"+nodeId+":"+zoneSettings.getName());
		
		return zone;
	}

	public Room createRoom(Zone zone, RoomSettings roomSettings) throws GameException {
		boolean isMMO = (roomSettings.mmoSettings != null) && (roomSettings.mmoSettings.isActive);
		CreateRoomSettings params;
		if (!isMMO)
			params = new CreateRoomSettings();
		else {
			params = new CreateMMORoomSettings();
		}
		params.setName(roomSettings.name);
		params.setGroupId(roomSettings.groupId);
		params.setPassword(roomSettings.password);
		params.setAutoRemoveMode(GameRoomRemoveMode.fromString(roomSettings.autoRemoveMode));
		params.setMaxUsers(roomSettings.maxUsers);
		params.setMaxSpectators(roomSettings.maxSpectators);
		params.setDynamic(roomSettings.isDynamic);
		params.setGame(roomSettings.isGame);
		params.setHidden(roomSettings.isHidden);

		if (isMMO) {
			Vec3D defaultAOI = MMOHelper.stringToVec3D(roomSettings.mmoSettings.defaultAOI,
					roomSettings.mmoSettings.forceFloats);
			Vec3D lowerMapLimit = MMOHelper.stringToVec3D(roomSettings.mmoSettings.lowerMapLimit,
					roomSettings.mmoSettings.forceFloats);
			Vec3D higherMapLimit = MMOHelper.stringToVec3D(roomSettings.mmoSettings.higherMapLimit,
					roomSettings.mmoSettings.forceFloats);

			CreateMMORoomSettings cmrs = (CreateMMORoomSettings) params;

			cmrs.setDefaultAOI(defaultAOI);
			cmrs.setUserMaxLimboSeconds(roomSettings.mmoSettings.userMaxLimboSeconds);
			cmrs.setProximityListUpdateMillis(roomSettings.mmoSettings.proximityListUpdateMillis);
			cmrs.setSendAOIEntryPoint(roomSettings.mmoSettings.sendAOIEntryPoint);

			if ((lowerMapLimit != null) && (higherMapLimit != null)) {
				cmrs.setMapLimits(new CreateMMORoomSettings.MapLimits(lowerMapLimit, higherMapLimit));
			}
		}
		Room room = this.server.getAPIManager().getSFSApi().createRoom(zone, params, null, false, null, false, false);

		return (Room) room;
	}

	public boolean isActive() {
		return true;
	}

}
