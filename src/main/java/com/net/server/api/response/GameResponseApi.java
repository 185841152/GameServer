package com.net.server.api.response;

import com.net.business.entity.AppUser;
import com.net.business.entity.GameData;
import com.net.engine.core.NetEngine;
import com.net.engine.data.TransportType;
import com.net.engine.io.IResponse;
import com.net.engine.io.Response;
import com.net.engine.sessions.ISession;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameArray;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.GameRoomSettings;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.exceptions.ErrorData;
import com.net.server.exceptions.ExceptionMessageComposer;
import com.net.server.exceptions.GameException;
import com.net.server.mmo.MMORoom;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameResponseApi implements IGameResponseApi {
	protected final NetEngine engine;
	private Logger logger = LoggerFactory.getLogger(GameResponseApi.class);

	public GameResponseApi(NetEngine engine) {
		this.engine = engine;
	}

	public void sendExtResponse(Object cmdName, IGameObject params, List<User> recipients, Room room, boolean sendUDP) {
		if (room != null) {
			params.putInt("r", room.getId());
		}
		// 在相同网关的用户进行合并
		Map<Integer, List<Integer>> gateUsers = new HashMap<Integer, List<Integer>>();
		for (User user : recipients) {
			List<Integer> uids = gateUsers.get(user.getSessionId());
			if (uids == null) {
				uids = new ArrayList<Integer>();
				gateUsers.put(user.getSessionId(), uids);
			}
			uids.add(user.getId());
		}

		Iterator<Integer> iterator = gateUsers.keySet().iterator();
		while (iterator.hasNext()) {
			Integer sessionId = iterator.next();

			ISession session = this.engine.getSessionManager().getLocalSessionById(sessionId);

			IResponse response = new Response();
			response.setId(cmdName);
			response.setContent(params);
			response.setRecipients(session);
			response.setUserIds(gateUsers.get(sessionId));

			if (sendUDP) {
				response.setTransportType(TransportType.UDP);
			}
			response.write();

		}
	}

	public void notifyRequestError(GameException err, User recipient, SystemRequest requestType) {
		notifyRequestError(err.getErrorData(), recipient, requestType);
	}

	public void notifyRequestError(ErrorData errData, User recipient, SystemRequest requestType) {
		if (recipient != null) {
			ISession session = engine.getSessionManager().getSessionById(recipient.getSessionId());
			if (session != null) {
				IGameObject resObj = GameObject.newInstance();

				IResponse response = new Response();
				response.setId(requestType.getId());
				response.setContent(resObj);
				response.setRecipients(session);
				response.setUserId(recipient.getId());

				resObj.putShort("ec", errData.getCode().getId());
				resObj.putUtfStringArray("ep", errData.getParams());

				response.write();
			}
		} else {
			ExceptionMessageComposer composer = new ExceptionMessageComposer(
					new NullPointerException("Can't send error notification toclient."));
			composer.setDescription("Attempting to send: " + errData.getCode() + " in response to: " + requestType);
			composer.setPossibleCauses("Recipient is NULL!");
			this.logger.warn(composer.toString());
		}
	}

	public void notifyJoinRoomSuccess(User recipient, Room joinedRoom) {
		ISession session = engine.getSessionManager().getLocalSessionById(recipient.getSessionId());
		if (session != null) {
			IGameObject resObj = GameObject.newInstance();
			resObj.putInt("r", joinedRoom.getId());
			resObj.putInt("status", (int)joinedRoom.getProperty("status"));
			resObj.putInt("idx", (int)recipient.getProperty("idx"));
			if (joinedRoom.getOwner()!=null) {
				resObj.putInt("ro", joinedRoom.getOwner().getId());
			}
			resObj.putGameArray("ul", (joinedRoom instanceof MMORoom) ? new GameArray() : joinedRoom.getUserListData());
			GameData data = (GameData) joinedRoom.getProperty("data");
			if(data != null){
				resObj.putGameObject("data",data.toGameObject());
			}
			IResponse response = new Response();
			response.setId(SystemRequest.JoinRoom.getId());
			response.setContent(resObj);
			response.setRecipients(session);
			response.setUserId(recipient.getId());
			response.write();
		}
	}

	public void notifyUserEnterRoom(User user, Room room) {
		if (!room.isFlagSet(GameRoomSettings.USER_ENTER_EVENT)) {
			return;
		}
		List<User> recipients = room.getUserList();
		recipients.remove(user);

		if (recipients.size() > 0) {
			AppUser appUser= (AppUser) user.getProperty("userInfo");
			PropertyFilter filter = new PropertyFilter() {
				@Override
				public boolean apply(Object object, String fieldName,
									 Object fieldValue) {
					return null == fieldValue;
				}
			};
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.setJsonPropertyFilter(filter);
			JSONObject obj=JSONObject.fromObject(appUser,jsonConfig);
			IGameObject userObj= GameObject.newFromJsonData(obj.toString());
			userObj.putInt("idx", (int)user.getProperty("idx"));

			sendExtResponse(SystemRequest.OnEnterRoom.getId(), userObj, recipients, room, false);
		}
	}
	
	public void notifyUserOnLineRoom(User user, Room room) {
		List<User> recipients = room.getUserList();
		recipients.remove(user);

		if (recipients.size() > 0) {
			IGameObject resObj=GameObject.newInstance();
			resObj.putInt("idx", (int)user.getProperty("idx"));
			sendExtResponse(SystemRequest.OnUserOnline.getId(), resObj, recipients, room, false);
		}
	}

	public void notifyUserExitRoom(User user, Room room, boolean sendToEveryOne) {
		if (!room.isFlagSet(GameRoomSettings.USER_EXIT_EVENT)) {
			return;
		}
		List<User> recipients = room.getUserList();
		recipients.remove(user);
		if (recipients.size() > 0) {
			IGameObject resObj = GameObject.newInstance();
			resObj.putInt("u", user.getId());
			resObj.putInt("r", room.getId());
			resObj.putInt("idx", (int)user.getProperty("idx"));
			sendExtResponse(SystemRequest.OnUserExitRoom.getId(), resObj, recipients, room, false);
		}
	}
	
	public void notifyRoomRemoved(Room room){
	    List<User> recipients = room.getUserList();

	    if (recipients.size() > 0){
	      IGameObject resObj = GameObject.newInstance();

	      resObj.putInt("r", room.getId());

	      sendExtResponse(SystemRequest.OnRoomLost.getId(), resObj, recipients, room, false);
	    }
	}
	
}