package com.net.engine.util.scheduling;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.net.engine.service.IService;

public class Scheduler implements IService, Runnable {
	private static AtomicInteger schedulerId = new AtomicInteger(0);

	private volatile int threadId = 1;

	private long SLEEP_TIME = 250L;
	private ExecutorService taskExecutor;
	private LinkedList<ScheduledTask> taskList;
	private LinkedList<ScheduledTask> addList;
	private String serviceName;
	private Logger logger;
	private volatile boolean running = false;

	public Scheduler() {
		this(null);
	}

	public Scheduler(Logger customLogger) {
		schedulerId.incrementAndGet();
		this.taskList = new LinkedList<ScheduledTask>();
		this.addList = new LinkedList<ScheduledTask>();

		if (this.logger == null)
			this.logger = LoggerFactory.getLogger("bootLogger");
		else
			this.logger = customLogger;
	}

	public Scheduler(long interval) {
		this();
		this.SLEEP_TIME = interval;
	}

	public void init(Object o) {
		startService();
	}

	public void destroy(Object o) {
		stopService();
	}

	public String getName() {
		return this.serviceName;
	}

	public void setName(String name) {
		this.serviceName = name;
	}

	public void handleMessage(Object message) {
		throw new UnsupportedOperationException("not supported in this class version");
	}

	public void startService() {
		this.running = true;
		this.taskExecutor = Executors.newSingleThreadExecutor();
		this.taskExecutor.execute(this);
	}

	public void stopService() {
		this.running = false;
		List<Runnable> leftOvers = this.taskExecutor.shutdownNow();

		this.taskExecutor = null;

		this.logger.info("Scheduler stopped. Unprocessed tasks: " + leftOvers.size());
	}

	public void run() {
		Thread.currentThread().setName("Scheduler" + schedulerId.get() + "-thread-" + this.threadId++);
		this.logger.info("Scheduler started: " + this.serviceName);

		while (this.running) {
			try {
				executeTasks();
				Thread.sleep(this.SLEEP_TIME);
			} catch (InterruptedException ie) {
				this.logger.warn("Scheduler: " + this.serviceName + " interrupted.");
			} catch (Exception e) {
			}
		}
	}

	public void addScheduledTask(Task task, int interval, boolean loop, ITaskHandler callback) {
		synchronized (this.addList) {
			this.addList.add(new ScheduledTask(task, interval, loop, callback));
		}
	}

	private void executeTasks() {
		long now = System.currentTimeMillis();

		if (this.taskList.size() > 0) {
			synchronized (this.taskList) {
				for (Iterator<ScheduledTask> it = this.taskList.iterator(); it.hasNext();) {
					ScheduledTask t = it.next();

					if (!t.task.isActive()) {
						it.remove();
					} else {
						if (now < t.expiry) {
							continue;
						}
						try {
							t.callback.doTask(t.task);
						} catch (Exception e) {
							logger.info("Scheduler callback exception. Callback: " + t.callback + ", Exception: " + e,
									e);
						}

						if (t.loop) {
							t.expiry += t.interval * 1000;
						} else {
							it.remove();
						}
					}
				}
			}

		}

		if (this.addList.size() > 0) {
			synchronized (this.taskList) {
				this.taskList.addAll(this.addList);
				this.addList.clear();
			}
		}
	}

	private final class ScheduledTask {
		long expiry;
		int interval;
		boolean loop;
		ITaskHandler callback;
		Task task;

		public ScheduledTask(Task t, int interval, boolean loop, ITaskHandler callback) {
			this.task = t;
			this.interval = interval;
			this.expiry = (System.currentTimeMillis() + interval * 1000);
			this.callback = callback;
			this.loop = loop;
		}

		@SuppressWarnings("unused")
		public int getInterval() {
			return this.interval;
		}

		@SuppressWarnings("unused")
		public Task getTask() {
			return this.task;
		}

		@SuppressWarnings("unused")
		public ITaskHandler getCallback() {
			return this.callback;
		}

		@SuppressWarnings("unused")
		public long getExpiry() {
			return this.expiry;
		}

		@SuppressWarnings("unused")
		public boolean isLooping() {
			return this.loop;
		}
	}
}