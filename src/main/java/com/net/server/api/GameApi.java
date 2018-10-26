package com.net.server.api;

import com.net.business.entity.GameData;
import com.net.business.vo.SeatingVo;
import com.net.engine.core.NetEngine;
import com.net.engine.io.IResponse;
import com.net.engine.io.Response;
import com.net.engine.sessions.ISession;
import com.net.server.GameServer;
import com.net.server.api.response.GameResponseApi;
import com.net.server.api.response.IGameResponseApi;
import com.net.server.config.DefaultConstants;
import com.net.server.controllers.SystemRequest;
import com.net.server.controllers.system.Login;
import com.net.server.core.ISFSEventParam;
import com.net.server.core.SFSEvent;
import com.net.server.core.SFSEventParam;
import com.net.server.core.SFSEventType;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.entities.GameRoomSettings;
import com.net.server.entities.Room;
import com.net.server.entities.User;
import com.net.server.entities.Zone;
import com.net.server.entities.managers.IUserManager;
import com.net.server.entities.variables.RoomVariable;
import com.net.server.entities.variables.UserVariable;
import com.net.server.entities.variables.Variables;
import com.net.server.exceptions.*;
import com.net.server.mmo.CreateMMORoomSettings;
import com.net.server.mmo.MMORoom;
import com.net.server.mmo.Vec3D;
import com.net.server.util.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameApi implements IGameApi {
    protected final GameServer server;
    protected final NetEngine engine;
    protected final Logger log;
    protected IUserManager globalUserManager;
    private final LoginErrorHandler loginErrorHandler;
    protected final IGameResponseApi responseAPI;

    public GameApi(GameServer server) {
        this.log = LoggerFactory.getLogger(getClass());
        this.server = server;
        this.engine = NetEngine.getInstance();
        this.globalUserManager = server.getUserManager();
        this.loginErrorHandler = new LoginErrorHandler();
        this.responseAPI = new GameResponseApi(this.engine);
    }

    public IGameResponseApi getResponseAPI() {
        return this.responseAPI;
    }

    public User getUserById(int userId) {
        return this.globalUserManager.getUserById(userId);
    }

    public User getUserByName(String name) {
        return this.globalUserManager.getUserByName(name);
    }

    public void sendExtensionResponse(Object cmdName, IGameObject params, List<User> recipients, Room room,
                                      boolean useUDP) {
        this.responseAPI.sendExtResponse(cmdName, params, recipients, room, useUDP);
    }

    public void sendExtensionResponse(Object cmdName, IGameObject params, User recipient, Room room, boolean useUDP) {
        List<User> msgRecipients = new LinkedList<User>();
        msgRecipients.add(recipient);

        this.responseAPI.sendExtResponse(cmdName, params, msgRecipients, room, useUDP);
    }

    public void removeRoom(Room room) {
        removeRoom(room, true, true);
    }

    public void removeRoom(Room room, boolean fireClientEvent, boolean fireServerEvent) {
        room.getZone().removeRoom(room);

        if (room.getOwner() != null) {
            room.getOwner().removeCreatedRoom(room);
        }

        if (fireClientEvent) {
            this.responseAPI.notifyRoomRemoved(room);
        }

        if (fireServerEvent) {
            Map<ISFSEventParam, Object> evtParams = new HashMap<ISFSEventParam, Object>();
            evtParams.put(SFSEventParam.ROOM, room);

            this.server.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.ROOM_REMOVED, evtParams));
        }
    }

    public boolean checkSecurePassword(ISession session, String originalPass, String encryptedPass) {
        if ((originalPass == null) || (originalPass.length() < 1)) {
            return false;
        }
        if ((encryptedPass == null) || (encryptedPass.length() < 1)) {
            return false;
        }

        return encryptedPass.equalsIgnoreCase(CryptoUtils.getClientPassword(session, originalPass));
    }

    public User login(ISession sender, String name, Integer userId, String ip, String token, int uid, String zoneName) {
        return login(sender, name, userId, ip, token, uid, zoneName, false);
    }

    public User login(ISession sender, String name, Integer userId, String ip, String token, int sessionId, String zoneName, boolean forceLogout) {
        IGameObject resObj = GameObject.newInstance();
        resObj.putInt(Login.KEY_SESSIONID, sessionId);

        User user = null;

        IResponse response = new Response();
        response.setId(SystemRequest.Login.getId());
        response.setContent(resObj);
        response.setRecipients(sender);
        response.setUserId(sessionId);

        Zone zone = this.server.getZoneManager().getZoneByName(zoneName);

        if (zone == null) {
            resObj.putShort("ec", ErrorCode.LOGIN_BAD_ZONENAME.getId());
            resObj.putUtfStringArray("ep", Arrays.asList(new String[]{zoneName}));

            response.write();

            this.log.info("Bad login request, Zone: " + zoneName + " does not exist. Requested by: " + sender);

            return null;
        }

        try {
            user = zone.login(sender, name, userId);
            this.log.info(String.format("User login: %s", new Object[]{user.toString()}));

            user.setLastLoginTime(System.currentTimeMillis());
            user.setIp(ip);

            if (user.getLastJoinedRoom() != null) {
                this.responseAPI.notifyUserOnLineRoom(user, user.getLastJoinedRoom());
                resObj.putInt("r", user.getLastJoinedRoom().getId());
                Room room = user.getLastJoinedRoom();
                int status = (int) room.getProperty("status");
                GameData gameData = (GameData) room.getProperty("data");
                if (gameData != null) {
                    resObj.putGameObject("data", gameData.toGameObject());
                }
                resObj.putInt("idx", (int) user.getProperty("idx"));
                resObj.putInt("status", status);
                if (room.getOwner() != null) {
                    resObj.putInt("ro", room.getOwner().getId());
                }
                resObj.putGameArray("ul", room.getUserListData());
            }
            resObj.putInt("i", user.getId());
            resObj.putUtfString("u", user.getName());
            resObj.putUtfString("ip", ip);
            resObj.putUtfString("z", zone.getName());
            resObj.putShort("rs", (short) zone.getUserReconnectionSeconds());

            response.write();

            Map<ISFSEventParam, Object> evtParams = new HashMap<ISFSEventParam, Object>();
            evtParams.put(SFSEventParam.USER, user);

            this.server.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_JOIN_ZONE, evtParams));
        } catch (SFSLoginException err) {
            this.log.info("Login error: " + err.getMessage() + ". Requested by: " + sender);
            this.loginErrorHandler.execute(sender, sessionId, err);
        }

        return user;
    }

    public Room createRoom(Zone zone, CreateRoomSettings params, User owner) throws SFSCreateRoomException {
        return createRoom(zone, params, owner, false, null, true, true);
    }

    public Room createRoom(Zone zone, CreateRoomSettings params, User owner, boolean joinIt, Room roomToLeave)
            throws SFSCreateRoomException {
        return createRoom(zone, params, owner, joinIt, roomToLeave, true, true);
    }

    public Room createRoom(Zone zone, CreateRoomSettings params, User owner, boolean joinIt, Room roomToLeave,
                           boolean fireClientEvent, boolean fireServerEvent) throws SFSCreateRoomException {
        Room theRoom = null;
        try {
            String groupId = params.getGroupId();
            if ((groupId == null) || (groupId.length() == 0)) {
                params.setGroupId(DefaultConstants.ZONE_DEFAULT_GROUP);
                throw new SFSCreateRoomException();
            }

            theRoom = zone.createRoom(params, owner);

            if (owner != null) {
                owner.addCreatedRoom(theRoom);
            }

            if ((theRoom instanceof MMORoom)) {
                configureMMORoom((MMORoom) theRoom, (CreateMMORoomSettings) params);
            }

            if (fireClientEvent) {
                // this.responseAPI.notifyRoomAdded(theRoom);
            }

            if (fireServerEvent) {
                Map<ISFSEventParam, Object> eventParams = new HashMap<ISFSEventParam, Object>();
                eventParams.put(SFSEventParam.ROOM, theRoom);

                this.server.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.ROOM_ADDED, eventParams));
            }
        } catch (SFSCreateRoomException err) {
            if (fireClientEvent) {
                this.responseAPI.notifyRequestError(err, owner, SystemRequest.CreateRoom);
            }
            String message = String.format("Room creation error. %s,%s", new Object[]{err.getMessage(), owner});
            throw new SFSCreateRoomException(message);
        }

        if ((theRoom != null) && (owner != null) && (joinIt)) {
            try {
                joinRoom(owner, theRoom, theRoom.getPassword(), false, roomToLeave, true, true);
            } catch (SFSJoinRoomException e) {
                this.log.warn("Unable to join the just created Room: " + theRoom + ", reason: " + e.getMessage());
            }
        }

        return theRoom;
    }

    public void joinRoom(User user, Room room) throws SFSJoinRoomException {
        joinRoom(user, room, "", false, user.getLastJoinedRoom());
    }

    public void joinRoom(User user, Room roomToJoin, String password, boolean asSpectator, Room roomToLeave)
            throws SFSJoinRoomException {
        joinRoom(user, roomToJoin, password, asSpectator, roomToLeave, true, true);
    }

    public void joinRoom(User user, Room roomToJoin, String password, boolean asSpectator, Room roomToLeave,
                         boolean fireClientEvent, boolean fireServerEvent) throws SFSJoinRoomException {
        boolean previousRoomIsMMO = false;
        try {
            if (user.isJoining()) {
                throw new SFSRuntimeException("Join request discarded. User is already in a join transaction: " + user);
            }
            user.setJoining(true);

            if (roomToJoin == null) {
                throw new SFSJoinRoomException("Requested room doesn't exist", new ErrorData(ErrorCode.JOIN_BAD_ROOM));
            }

            if (!roomToJoin.isActive()) {
                String message = String.format("Room is currently locked, %s", new Object[]{roomToJoin});
                ErrorData errData = new ErrorData(ErrorCode.JOIN_ROOM_LOCKED);
                errData.addParameter(roomToJoin.getName());

                throw new SFSJoinRoomException(message, errData);
            }

            boolean isMMO = roomToJoin instanceof MMORoom;
            previousRoomIsMMO = (roomToLeave != null) && ((roomToLeave instanceof MMORoom));

            if (isMMO) {
                Room previousMMORoom = checkMultiMMOJoin(user, roomToJoin, roomToLeave);
                if (previousMMORoom != null) {
                    throw new SFSJoinRoomException("Cannot join another MMORoom. Multi MMORoom join is not supported. User is already joined in: " + previousMMORoom);
                }

                user.setProperty("_uJoinTime", Long.valueOf(System.currentTimeMillis()));

                if ((previousRoomIsMMO) && (user.getLastProxyList() != null)) {
                    user.setProperty("PreviousMMORoomState", new MMORoom.PreviousMMORoomState(roomToLeave.getId(), user.getLastProxyList()));
                }

            }

            boolean doorIsOpen = true;
            if (roomToJoin.isPasswordProtected()) {
                doorIsOpen = roomToJoin.getPassword().equals(password);
            }

            if (!doorIsOpen) {
                String message = String.format("Room password is wrong, %s", new Object[]{roomToJoin});
                ErrorData data = new ErrorData(ErrorCode.JOIN_BAD_PASSWORD);
                data.addParameter(roomToJoin.getName());

                throw new SFSJoinRoomException(message, data);
            }
            SeatingVo seatingVo = (SeatingVo) roomToJoin.getProperty("seating");
//			user.setProperty("idx",roomToJoin.getUserList().size());
            user.setProperty("idx", seatingVo.addUser(user.getId()));
            roomToJoin.addUser(user, asSpectator);
            if (this.server.getConfigurator().getServerSettings().statsExtraLoggingEnabled) {
                this.log.info(String.format("Room joined: %s, %s, asSpect: %s", new Object[]{roomToJoin, user, Boolean.valueOf(asSpectator)}));
            }
            if (fireClientEvent) {
                this.responseAPI.notifyJoinRoomSuccess(user, roomToJoin);
                if (!isMMO) {
                    this.responseAPI.notifyUserEnterRoom(user, roomToJoin);
                }
            }

            if (fireServerEvent) {
                Map<ISFSEventParam, Object> evtParams = new HashMap<ISFSEventParam, Object>();
                evtParams.put(SFSEventParam.ROOM, roomToJoin);
                evtParams.put(SFSEventParam.USER, user);

                this.server.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_JOIN_ROOM, evtParams));
            }

            if (roomToLeave != null) {
                leaveRoom(user, roomToLeave);
            }
        } catch (SFSJoinRoomException err) {
            if (fireClientEvent) {
                this.responseAPI.notifyRequestError(err, user, SystemRequest.JoinRoom);
            }

            throw err;
        } finally {
            user.setJoining(false);

            if (previousRoomIsMMO) {
                user.removeProperty("PreviousMMORoomState");
            }

        }
    }

    public void leaveRoom(User user, Room room) {
        leaveRoom(user, room, true, true);
    }

    public void leaveRoom(User user, Room room, boolean fireClientEvent, boolean fireServerEvent) {
        if (room == null) {
            room = user.getLastJoinedRoom();

            if (room == null) {
                throw new SFSRuntimeException("LeaveRoom failed: user is not joined in any room. " + user);
            }

        }

        if (!room.containsUser(user)) {
            return;
        }

        Zone zone = user.getZone();
        zone.removeUserFromRoom(user, room);

        //移除座位号
        SeatingVo seatingVo = (SeatingVo) room.getProperty("seating");
        seatingVo.removeUser(user.getId());

        if (fireClientEvent) {
            this.responseAPI.notifyUserExitRoom(user, room, room.isFlagSet(GameRoomSettings.USER_EXIT_EVENT));
        }

        if (fireServerEvent) {
            Map<ISFSEventParam, Object> evtParams = new HashMap<ISFSEventParam, Object>();
            evtParams.put(SFSEventParam.ROOM, room);
            evtParams.put(SFSEventParam.USER, user);

            this.server.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_LEAVE_ROOM, evtParams));
        }
    }

    public void setRoomVariables(User user, Room targetRoom, List<RoomVariable> variables) {
        setRoomVariables(user, targetRoom, variables, true, true, false);
    }

    public void setRoomVariables(User user, Room targetRoom, List<RoomVariable> variables, boolean fireClientEvent,
                                 boolean fireServerEvent, boolean overrideOwnership) {
        if (targetRoom == null) {
            throw new SFSRuntimeException("The target Room is null!");
        }
        if (variables == null) {
            throw new SFSRuntimeException("Missing variables list!");
        }

        List<RoomVariable> listOfChanges = new ArrayList<RoomVariable>();

        for (RoomVariable var : variables) {
            try {
                targetRoom.setVariable(var, overrideOwnership);
                listOfChanges.add(var);
            } catch (SFSVariableException e) {
                this.log.warn(e.getMessage());
            }

        }

        if ((listOfChanges.size() > 0) && (fireClientEvent)) {
            // this.responseAPI.notifyRoomVariablesUpdate(targetRoom,
            // listOfChanges);
        }

        if ((listOfChanges.size() > 0) && (fireServerEvent)) {
            Map<ISFSEventParam, Object> evtParams = new HashMap<ISFSEventParam, Object>();
            evtParams.put(SFSEventParam.ROOM, targetRoom);
            evtParams.put(SFSEventParam.USER, user);
            evtParams.put(SFSEventParam.VARIABLES, listOfChanges);
            evtParams.put(SFSEventParam.VARIABLES_MAP, Variables.toVariablesMap(listOfChanges));

            this.server.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.ROOM_VARIABLES_UPDATE, evtParams));
        }
    }

    public void setUserVariables(User owner, List<UserVariable> variables) {
        setUserVariables(owner, variables, true, true);
    }

    public void setUserVariables(User owner, List<UserVariable> variables, boolean fireClientEvent,
                                 boolean fireServerEvent) {
        List<UserVariable> listOfChanges = executeSetUserVariables(owner, variables);

        fireUserVariablesEvent(owner, listOfChanges, null, fireClientEvent, fireServerEvent);
    }

    void setUserVariables(User owner, List<UserVariable> variables, Vec3D aoi, boolean fireClientEvent,
                          boolean fireServerEvent) {
        List<UserVariable> listOfChanges = executeSetUserVariables(owner, variables);

        fireUserVariablesEvent(owner, listOfChanges, aoi, fireClientEvent, fireServerEvent);
    }

    private List<UserVariable> executeSetUserVariables(User owner, List<UserVariable> variables) {
        if (owner == null) {
            throw new SFSRuntimeException("The User is null!");
        }
        if (variables == null) {
            throw new SFSRuntimeException("Missing variables list!");
        }
        List<UserVariable> listOfChanges = new ArrayList<UserVariable>();

        for (UserVariable var : variables) {
            try {
                owner.setVariable(var);

                if (!var.isHidden())
                    listOfChanges.add(var);
            } catch (SFSVariableException e) {
                this.log.warn(e.getMessage());
            }
        }

        return listOfChanges;
    }

    private void fireUserVariablesEvent(User owner, List<UserVariable> listOfChanges, Vec3D aoi,
                                        boolean fireClientEvent, boolean fireServerEvent) {
        if ((listOfChanges.size() > 0) && (fireClientEvent)) {
            // this.responseAPI.notifyUserVariablesUpdate(owner, listOfChanges,
            // aoi);
        }

        if ((listOfChanges.size() > 0) && (fireServerEvent)) {
            Map<ISFSEventParam, Object> evtParams = new HashMap<ISFSEventParam, Object>();
            evtParams.put(SFSEventParam.USER, owner);
            evtParams.put(SFSEventParam.VARIABLES, listOfChanges);
            evtParams.put(SFSEventParam.VARIABLES_MAP, Variables.toVariablesMap(listOfChanges));

            this.server.getEventManager().dispatchEvent(new SFSEvent(SFSEventType.USER_VARIABLES_UPDATE, evtParams));
        }
    }

    private void configureMMORoom(MMORoom room, CreateMMORoomSettings settings) {
        if (settings.getMapLimits() != null) {
            room.setMapLimits(settings.getMapLimits().getLowerLimit(), settings.getMapLimits().getHigherLimit());
        }
        room.setUserLimboMaxSeconds(settings.getUserMaxLimboSeconds());
        room.setSendAOIEntryPoint(settings.isSendAOIEntryPoint());
    }

    private Room checkMultiMMOJoin(User user, Room roomToJoin, Room roomToLeave) {
        Room previousMMORoom = null;

        Room anotherMMORoom = user.getCurrentMMORoom();

        if (anotherMMORoom != null) {
            if (anotherMMORoom != roomToLeave) {
                previousMMORoom = anotherMMORoom;
            }
        }
        return previousMMORoom;
    }
}