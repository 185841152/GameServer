package com.net.server.api;

import com.net.engine.io.IResponse;
import com.net.engine.io.Response;
import com.net.engine.sessions.ISession;
import com.net.server.controllers.SystemRequest;
import com.net.server.controllers.system.Login;
import com.net.server.data.GameObject;
import com.net.server.data.IGameObject;
import com.net.server.exceptions.ErrorCode;
import com.net.server.exceptions.ErrorData;
import com.net.server.exceptions.SFSLoginException;

public final class LoginErrorHandler {
	public void execute(ISession sender,int sessionId,SFSLoginException err) {
		IGameObject resObj = GameObject.newInstance();

		if (err.getErrorData() == null) {
			ErrorData errData = new ErrorData(ErrorCode.GENERIC_ERROR);
			errData.addParameter("An unexpected error occurred, please check the server side logs");

			err = new SFSLoginException(err.getMessage(), errData);
		}

		IResponse response = new Response();
		response.setId(SystemRequest.Login.getId());
		response.setContent(resObj);
		response.setRecipients(sender);

		resObj.putShort("ec", err.getErrorData().getCode().getId());
		resObj.putUtfStringArray("ep", err.getErrorData().getParams());
		resObj.putInt(Login.KEY_SESSIONID, sessionId);
		
		response.write();
	}
}