package com.net.engine.core;

import com.net.engine.io.IResponse;
import com.net.engine.util.scheduling.ITaskHandler;
import com.net.engine.util.scheduling.Task;

public final class EngineDelayedTaskHandler extends AbstractMethodDispatcher implements ITaskHandler {
	public EngineDelayedTaskHandler() {
		registerTasks();
	}

	private void registerTasks() {
		registerMethod("delayedSocketWrite", "onDelayedSocketWrite");
		registerMethod("RESTART", "onRestart");
	}

	public void doTask(Task task) throws Exception {
		try {
			callMethod((String) task.getId(), new Object[] { task });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onRestart(Object o) {
		NetEngine.getInstance().restart();
	}

	public void onDelayedSocketWrite(Task o) {
		Task task = (Task) o;
		IResponse response = (IResponse) task.getParameters().get("response");

		if (response != null)
			response.write();
	}
}