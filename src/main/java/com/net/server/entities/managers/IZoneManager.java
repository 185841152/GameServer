package com.net.server.entities.managers;

import java.util.List;

import com.net.server.config.ZoneSettings;
import com.net.server.core.ICoreService;
import com.net.server.entities.Room;
import com.net.server.entities.Zone;
import com.net.server.exceptions.GameException;

public interface IZoneManager extends ICoreService{
	public abstract List<Zone> getZoneList();

	public abstract Zone getZoneByName(String paramString);

	public abstract Zone getZoneById(int paramInt);

	public abstract void initializeZones() throws GameException;

	public abstract void addZone(Zone paramZone);

	public abstract Zone createZone(ZoneSettings paramZoneSettings) throws GameException;

	public abstract Room createRoom(Zone paramZone, ZoneSettings.RoomSettings paramRoomSettings) throws GameException;
}
