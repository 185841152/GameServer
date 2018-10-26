package com.net.server.controllers.system;

import com.net.engine.io.IRequest;
import com.net.server.controllers.BaseControllerCommand;
import com.net.server.controllers.SystemRequest;
import com.net.server.data.IGameObject;
import com.net.server.exceptions.SFSRequestValidationException;

public class Login extends BaseControllerCommand {
	public static final String KEY_USERNAME = "u";
	public static final String KEY_USERID = "ui";
	public static final String KEY_SESSIONID="si";
	public static final String KEY_ZONENAME = "z";
	public static final String DEFAULT_ZONE="BasicExamples";
	public static final String KEY_TOKEN="t";
	public static final String KEY_IP="ip";

	public Login() {
		super(SystemRequest.Login);
	}

	public boolean validate(IRequest request) throws SFSRequestValidationException {
		boolean res = true;
		IGameObject sfso = (IGameObject) request.getContent();
		if ((!sfso.containsKey(KEY_USERNAME))  || (!sfso.containsKey(KEY_SESSIONID))) {
			throw new SFSRequestValidationException("用户名和ID不能为空");
		}
		if (!sfso.containsKey(KEY_ZONENAME)) {
			sfso.putUtfString(KEY_ZONENAME, DEFAULT_ZONE);
		}

		return res;
	}

	public void execute(IRequest request) throws Exception {
		IGameObject reqObj = (IGameObject) request.getContent();

		String zoneName = reqObj.getUtfString(KEY_ZONENAME);
		String userName = reqObj.getUtfString(KEY_USERNAME);
		String token=reqObj.getUtfString(KEY_TOKEN);
		String ip=reqObj.getUtfString(KEY_IP);
		Integer userId=reqObj.getInt(KEY_USERID);
		int sessionId = reqObj.getInt(KEY_SESSIONID);
		

		this.api.login(request.getSender(), userName,userId,ip,token,sessionId, zoneName, true);
	}
}