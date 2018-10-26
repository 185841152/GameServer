package com.net.business.extensions.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.server.core.ISFSEvent;
import com.net.server.core.SFSEventType;
import com.net.server.data.IGameObject;
import com.net.server.entities.User;
import com.net.server.exceptions.SFSRuntimeException;

public abstract class GameExtension extends BaseGameExtension {
	public static final String MULTIHANDLER_REQUEST_ID = "__[[REQUEST_ID]]__";
	private final IHandlerFactory handlerFactory;

	public GameExtension() {
		this.handlerFactory = new GameHandlerFactory(this);
	}

	public void destroy() {
		this.handlerFactory.clearAll();
	}

	protected void addRequestHandler(Object requestId, Class<?> theClass) {
		if (!IClientRequestHandler.class.isAssignableFrom(theClass)) {
			throw new SFSRuntimeException(
					String.format("Provided Request Handler does not implement IClientRequestHandler: %s, Cmd: %s",
							new Object[] { theClass, requestId }));
		}

		this.handlerFactory.addHandler(requestId, theClass);
	}

	protected void addRequestHandler(Object requestId, IClientRequestHandler requestHandler) {
		this.handlerFactory.addHandler(requestId, requestHandler);
	}

	protected void addEventHandler(SFSEventType eventType, Class<?> theClass) {
		if (!IServerEventHandler.class.isAssignableFrom(theClass)) {
			throw new SFSRuntimeException(
					String.format("Provided Event Handler does not implement IServerEventHandler: %s, Cmd: %s",
							new Object[] { theClass, eventType.toString() }));
		}

		addEventListener(eventType, this);

		this.handlerFactory.addHandler(eventType.toString(), theClass);
	}

	protected void addEventHandler(SFSEventType eventType, IServerEventHandler handler) {
		addEventListener(eventType, this);

		this.handlerFactory.addHandler(eventType.toString(), handler);
	}

	protected void removeRequestHandler(String requestId) {
		this.handlerFactory.removeHandler(requestId);
	}

	protected void removeEventHandler(SFSEventType eventType) {
		removeEventListener(eventType, this);
		this.handlerFactory.removeHandler(eventType.toString());
	}

	protected void clearAllHandlers() {
		this.handlerFactory.clearAll();
	}

	public void handleClientRequest(Object requestId, User sender, IGameObject params) throws Exception {
		try {
			IClientRequestHandler handler = (IClientRequestHandler) this.handlerFactory.findHandler(requestId);

			if (handler == null) {
				throw new SFSRuntimeException("Request handler not found: '" + requestId
						+ "'. Make sure the handler is registered in your extension using addRequestHandler()");
			}
			handler.handleClientRequest(sender, params);
		} catch (InstantiationException err) {
			trace(ExtensionLogLevel.WARN, new Object[] { "Cannot instantiate handler class: " + err });
		} catch (IllegalAccessException err) {
			trace(ExtensionLogLevel.WARN, new Object[] { "Illegal access for handler class: " + err });
		}
	}

	public void handleServerEvent(ISFSEvent event) throws Exception {
		String handlerId = event.getType().toString();
		try {
			IServerEventHandler handler = (IServerEventHandler) this.handlerFactory.findHandler(handlerId);
			handler.handleServerEvent(event);
		} catch (InstantiationException err) {
			trace(ExtensionLogLevel.WARN, new Object[] { "Cannot instantiate handler class: " + err });
		} catch (IllegalAccessException err) {
			trace(ExtensionLogLevel.WARN, new Object[] { "Illegal access for handler class: " + err });
		}
	}

}