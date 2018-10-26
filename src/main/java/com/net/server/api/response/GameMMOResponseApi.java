package com.net.server.api.response;

import java.util.LinkedList;
import java.util.List;

import com.net.engine.io.IResponse;
import com.net.engine.io.Response;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.GameArray;
import com.net.server.data.GameObject;
import com.net.server.data.IGameArray;
import com.net.server.data.IGameObject;
import com.net.server.entities.GameRoom;
import com.net.server.entities.User;
import com.net.server.mmo.BaseMMOItem;
import com.net.server.mmo.IMMOItemVariable;
import com.net.server.mmo.MMOHelper;
import com.net.server.mmo.MMORoom;
import com.net.server.mmo.MMOUpdateDelta;
import com.net.server.mmo.Vec3D;

public class GameMMOResponseApi implements IGameMMOResponseApi {
	public void notifyProximityListUpdate(GameRoom room, MMOUpdateDelta delta) {
		MMORoom mmoRoom = (MMORoom) room;

		IGameObject sfso = new GameObject();
		sfso.putInt("r", mmoRoom.getId());

		if ((delta.getMinusUserList() != null) && (delta.getMinusUserList().size() > 0)) {
			List<Integer> minusList = new LinkedList<Integer>();
			for (User item : delta.getMinusUserList()) {
				minusList.add(item.getId());
			}
			sfso.putIntArray("m", minusList);
		}

		if ((delta.getPlusUserList() != null) && (delta.getPlusUserList().size() > 0)) {
			IGameArray plusList = new GameArray();
			for (User item : delta.getPlusUserList()) {
				IGameArray encodedUser = item.toGameArray();
				if (mmoRoom.isSendAOIEntryPoint()) {
					addExtraEntryPoint(encodedUser, item);
				}
				plusList.addGameArray(encodedUser);
			}

			sfso.putGameArray("p", plusList);
		}

		if ((delta.getMinusItemList() != null) && (delta.getMinusItemList().size() > 0)) {
			List<Integer> minusList = new LinkedList<Integer>();
			for (BaseMMOItem item : delta.getMinusItemList()) {
				minusList.add(item.getId());
			}
			sfso.putIntArray("n", minusList);
		}

		if ((delta.getPlusItemList() != null) && (delta.getPlusItemList().size() > 0)) {
			IGameArray plusList = new GameArray();
			for (BaseMMOItem item : delta.getPlusItemList()) {
				IGameArray encodedItem = item.toSFSArray();
				if (mmoRoom.isSendAOIEntryPoint()) {
					addExtraEntryPoint(encodedItem, item);
				}
				plusList.addGameArray(encodedItem);
			}

			sfso.putGameArray("q", plusList);
		}

		IResponse response = new Response();
		response.setId(SystemRequest.SetUserPosition.getId());
		response.setContent(sfso);
//		response.setRecipients(delta.getRecipient().getSession());

		response.write();
	}

	public void notifyItemVariablesUpdate(BaseMMOItem item, List<IMMOItemVariable> listOfChanges) {
		MMORoom mmoRoom = item.getRoom();

		IGameObject sfso = new GameObject();
		sfso.putInt("i", item.getId());
		sfso.putInt("r", mmoRoom.getId());

		IGameArray encodedVars = new GameArray();

		for (IMMOItemVariable var : listOfChanges) {
			if (var.isHidden()) {
				continue;
			}
			encodedVars.addGameArray(var.toSFSArray());
		}

		sfso.putGameArray("v", encodedVars);

//		List<User> users = mmoRoom.getProximityList(MMOHelper.getMMOItemLocation(item));
//		List<ISession> recipients = new LinkedList<ISession>();

//		for (User user : users) {
//			recipients.add(user.getSession());
//		}

		 IResponse response = new Response();
		 response.setId(SystemRequest.OnMMOItemVariablesUpdate.getId());
		 response.setContent(sfso);
//		 response.setRecipients(recipients);
		
		 response.write();
	}

	private void addExtraEntryPoint(IGameArray encodedUser, User target) {
		Vec3D pos = (Vec3D) target.getProperty("_uLoc");

		if (pos != null) {
			if (pos.isFloat())
				encodedUser.addFloatArray(pos.toFloatArray());
			else
				encodedUser.addIntArray(pos.toIntArray());
		}
	}

	private void addExtraEntryPoint(IGameArray encodedItem, BaseMMOItem target) {
		Vec3D pos = MMOHelper.getMMOItemLocation(target);

		if (pos != null) {
			if (pos.isFloat())
				encodedItem.addFloatArray(pos.toFloatArray());
			else
				encodedItem.addIntArray(pos.toIntArray());
		}
	}
}