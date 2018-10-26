package com.net.server.controllers;

public enum SystemRequest{
	  Handshake(Short.valueOf("0"),new Byte("0")), 
	  Login(Short.valueOf("1"),new Byte("0")), 
	  Logout(Short.valueOf("2"),new Byte("0")), 
	  GetRoomList(Short.valueOf("4"),new Byte("0")), 
	  JoinRoom(Short.valueOf("5"),new Byte("1")), 
	  AutoJoin(Short.valueOf("6"),new Byte("1")), 
	  CreateRoom(Short.valueOf("7"),new Byte("1")), 
	  GenericMessage(Short.valueOf("8"),new Byte("0")), 
	  ChangeRoomName(Short.valueOf("9"),new Byte("0")), 
	  ChangeRoomPassword(Short.valueOf("10"),new Byte("0")), 
	  ObjectMessage(Short.valueOf("11"),new Byte("0")), 
	  SetRoomVariables(Short.valueOf("12"),new Byte("0")), 
	  SetUserVariables(Short.valueOf("13"),new Byte("0")), 
	  CallExtension(Short.valueOf("14"),new Byte("0")), 
	  LeaveRoom(Short.valueOf("15"),new Byte("1")), 
	  SubscribeRoomGroup(Short.valueOf("16"),new Byte("0")), 
	  UnsubscribeRoomGroup(Short.valueOf("17"),new Byte("0")), 
	  SpectatorToPlayer(Short.valueOf("18"),new Byte("0")), 
	  PlayerToSpectator(Short.valueOf("19"),new Byte("0")), 
	  ChangeRoomCapacity(Short.valueOf("20"),new Byte("0")), 
	  PublicMessage(Short.valueOf("21"),new Byte("1")), 
	  PrivateMessage(Short.valueOf("22"),new Byte("0")), 
	  ModeratorMessage(Short.valueOf("23"),new Byte("0")), 
	  AdminMessage(Short.valueOf("24"),new Byte("0")), 
	  KickUser(Short.valueOf("25"),new Byte("0")), 
	  BanUser(Short.valueOf("26"),new Byte("0")), 
	  ManualDisconnection(Short.valueOf("27"),new Byte("0")), 
	  FindRooms(Short.valueOf("28"),new Byte("0")), 
	  FindUsers(Short.valueOf("29"),new Byte("0")), 
	  PingPong(Short.valueOf("30"),new Byte("0")), 
	  SetUserPosition(Short.valueOf("31"),new Byte("0")), 
	
	  OnEnterRoom(Short.valueOf("1000"),new Byte("0")),
	  OnRoomCountChange(Short.valueOf("1001"),new Byte("0")), 
	  OnUserLost(Short.valueOf("1002"),new Byte("1")), 
	  OnRoomLost(Short.valueOf("1003"),new Byte("0")), 
	  OnUserExitRoom(Short.valueOf("1004"),new Byte("0")), 
	  OnClientDisconnection(Short.valueOf("1005"),new Byte("0")), 
	  OnReconnectionFailure(Short.valueOf("1006"),new Byte("0")), 
	  OnMMOItemVariablesUpdate(Short.valueOf("1007"),new Byte("0")),
	  OnReconnectionSuccess(Short.valueOf("1008"),new Byte("1")), 
	  OnUserOnline(Short.valueOf("1009"),new Byte("1")), 
	  OnUserAddOrder(Short.valueOf("1009"),new Byte("1")),

	  OnUserAttack(Short.valueOf("2000"),new Byte("1")),
	  StartGame(Short.valueOf("2001"),new Byte("1")),
	  OutMonster(Short.valueOf("2002"),new Byte("1")),
	  NextChapter(Short.valueOf("2003"),new Byte("1")),
	  GameFinish(Short.valueOf("2004"),new Byte("1")),
	  GameOver(Short.valueOf("2005"),new Byte("1")),
	  KillMonster(Short.valueOf("2007"),new Byte("1")),
	  StopGame(Short.valueOf("2008"),new Byte("1")),
	  UseTool(Short.valueOf("2009"),new Byte("1")),
	  ReplayGame(Short.valueOf("2010"),new Byte("1")),
	  ContinueGame(Short.valueOf("2011"),new Byte("1"))
	  ;

	private Object id;
	private Object type;

	public static SystemRequest fromId(Object id) {
		SystemRequest req = null;

		for (SystemRequest item : values()) {
			if (!item.getId().equals(id))
				continue;
			req = item;
			break;
		}
		return req;
	}

	private SystemRequest(Object id,Object type) {
		this.id = id;
		this.type=type;
	}

	public Object getId() {
		return this.id;
	}
	
	public Object getType(){
		return this.type;
	}

}