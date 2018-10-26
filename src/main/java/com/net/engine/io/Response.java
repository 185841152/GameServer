package com.net.engine.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.net.engine.core.NetEngine;
import com.net.engine.data.TransportType;
import com.net.engine.sessions.ISession;
import com.net.engine.util.scheduling.Scheduler;
import com.net.engine.util.scheduling.Task;

public class Response extends AbstractEngineMessage implements IResponse {
	private List<Integer> userIds;
	private ISession recipients;
	private TransportType type;

	public Response() {
		this.type = TransportType.TCP;
	}

	public ISession getRecipients() {
		return this.recipients;
	}

	public TransportType getTransportType() {
		return this.type;
	}

	public boolean isTCP() {
		return this.type == TransportType.TCP;
	}

	public boolean isUDP() {
		return this.type == TransportType.UDP;
	}

	public void setRecipients(Collection<ISession> recipents) {
		
	}

	public void setRecipients(ISession session) {
		this.recipients = session;
	}

	public void setTransportType(TransportType type) {
		this.type = type;
	}

	public void write() {
		NetEngine.getInstance().write(this);
	}

	public void write(int delay) {
		Scheduler scheduler = (Scheduler) NetEngine.getInstance().getServiceByName("scheduler");

		Task delayedSocketWriteTask = new Task("delayedSocketWrite");
		delayedSocketWriteTask.getParameters().put("response", this);

		scheduler.addScheduledTask(delayedSocketWriteTask, delay, false,
				NetEngine.getInstance().getEngineDelayedTaskHandler());
	}

	public static IResponse clone(IResponse original) {
		IResponse newResponse = new Response();
		newResponse.setContent(original.getContent());
		newResponse.setId(original.getId());
		newResponse.setTransportType(original.getTransportType());

		return newResponse;
	}

	public List<Integer> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<Integer> userIds) {
		this.userIds = userIds;
	}
	
	public void setUserId(Integer userId){
		List<Integer> uids=new ArrayList<Integer>();
		uids.add(userId);
		this.userIds=uids;
	}

}