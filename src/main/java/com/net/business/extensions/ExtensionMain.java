package com.net.business.extensions;

import com.net.business.extensions.core.GameExtension;
import com.net.business.extensions.handler.OnUserLostHandler;
import com.net.business.extensions.handler.OnUserReconnectionSuccessHandler;
import com.net.business.extensions.handler.room.*;
import com.net.business.extensions.listener.ServerLogOutListener;
import com.net.business.extensions.listener.ServerLoginListener;
import com.net.business.extensions.listener.ServerStartListener;
import com.net.business.task.DayliyClear;
import com.net.server.GameServer;
import com.net.server.controllers.SystemRequest;
import com.net.server.core.SFSEventType;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ExtensionMain extends GameExtension {

	@Override
	public void init() {
		addRequestHandler(SystemRequest.PublicMessage.getId(), new PublicMessageHandler());
		addRequestHandler(SystemRequest.OnReconnectionSuccess.getId(), new OnUserReconnectionSuccessHandler());
		addRequestHandler(SystemRequest.OnUserLost.getId(), new OnUserLostHandler());
		addRequestHandler(SystemRequest.CreateRoom.getId(), new CreateRoomHandler());
		addRequestHandler(SystemRequest.JoinRoom.getId(), new JoinRoomHandler());
		addRequestHandler(SystemRequest.OnUserAttack.getId(), new UserAttackHandler());
		addRequestHandler(SystemRequest.StartGame.getId(), new StartGameHandler());
		addRequestHandler(SystemRequest.KillMonster.getId(), new KillMonsterHandler());
		addRequestHandler(SystemRequest.UseTool.getId(),new UseToolHandler());
		addRequestHandler(SystemRequest.StopGame.getId(),new StopGameHandler());
		addRequestHandler(SystemRequest.ReplayGame.getId(),new ReplayGameHandler());
		addRequestHandler(SystemRequest.NextChapter.getId(),new NextChapterHandler());
		addRequestHandler(SystemRequest.LeaveRoom.getId(),new LeaveRoomHandler());
		addRequestHandler(SystemRequest.ContinueGame.getId(),new ContinueGameHandler());

		addEventHandler(SFSEventType.SERVER_READY, new ServerStartListener());
		addEventHandler(SFSEventType.USER_JOIN_ZONE, new ServerLoginListener());
		addEventHandler(SFSEventType.USER_DISCONNECT, new ServerLogOutListener());
		Calendar calendar=Calendar.getInstance();
		int min=calendar.get(Calendar.MINUTE);
		GameServer.getInstance().getSystemThreadPool().scheduleWithFixedDelay(new DayliyClear(), 60-min, 60, TimeUnit.MINUTES);
	}
}
