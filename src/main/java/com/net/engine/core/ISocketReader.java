package com.net.engine.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.controllers.IController;
import com.net.engine.controllers.IControllerManager;
import com.net.engine.exceptions.RequestQueueFullException;
import com.net.engine.io.IProtocolCodec;
import com.net.engine.io.IRequest;

public abstract class ISocketReader implements IProtocolCodec {
	protected final IControllerManager controllerManager;
	protected final NetEngine engine;
	protected final Logger logger;

	public ISocketReader() {
		this.logger = LoggerFactory.getLogger(getClass());
		this.engine = NetEngine.getInstance();
		this.controllerManager = this.engine.getControllerManager();
	}

	protected void dispatchRequestToController(IRequest request, Object controllerId) {
		if (controllerId == null) {
			throw new IllegalStateException("Invalid Request: missing controllerId -> " + request);
		}
		IController controller = this.controllerManager.getControllerById(controllerId);
		try {
			controller.enqueueRequest(request);
		} catch (RequestQueueFullException err) {
			this.logger
					.error(String.format("RequestQueue is full (%s/%s). Controller ID: %s, Dropping incoming request: ",
							new Object[] { Integer.valueOf(controller.getQueueSize()),
									Integer.valueOf(controller.getMaxQueueSize()), controllerId.toString(),
									request.toString() }));
		} catch (NullPointerException err) {
			this.logger.warn("Can't handle this request! The related controller is not found: " + controllerId
					+ ", Request: " + request);
		}
	}

}