package com.net.server.api.response;

import java.util.List;

import com.net.server.entities.GameRoom;
import com.net.server.mmo.BaseMMOItem;
import com.net.server.mmo.IMMOItemVariable;
import com.net.server.mmo.MMOUpdateDelta;

public abstract interface IGameMMOResponseApi {
	public abstract void notifyProximityListUpdate(GameRoom paramRoom, MMOUpdateDelta paramMMOUpdateDelta);

	public abstract void notifyItemVariablesUpdate(BaseMMOItem paramBaseMMOItem, List<IMMOItemVariable> paramList);
}