package com.net.server.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SFSRestart extends Thread {
	private static final String LINUX_LAUNCHER = "./sfs2x.sh";
	private static final String OSX_LAUNCHER = "./sfs2x.sh";
	private static final String WIN_LAUNCHER = "sfs2x.bat";
	private final Logger log;
	private boolean isWindows;
	private boolean isOSX;
	private boolean isLinux = false;

	public SFSRestart() {
		setName(":::SFSRestarter:::");
		this.log = LoggerFactory.getLogger(SFSRestart.class);

		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("linux") != -1)
			this.isLinux = true;
		else if (osName.toLowerCase().indexOf("mac os x") != -1)
			this.isOSX = true;
		else if (osName.toLowerCase().indexOf("windows") != -1)
			this.isWindows = true;
		else
			throw new IllegalStateException("Restart failure: operating system not supported: " + osName);
	}

	public void run() {
		try {
			String restartCmd = null;
			if (this.isWindows)
				restartCmd = WIN_LAUNCHER;
			else if (this.isLinux)
				restartCmd = LINUX_LAUNCHER;
			else if (this.isOSX) {
				restartCmd = OSX_LAUNCHER;
			}

			String[] cmds = restartCmd.split("\\,");
			List<String> command = new ArrayList<String>();
			for (String cmd : cmds) {
				command.add(cmd);
			}
			ProcessBuilder builder = new ProcessBuilder(command);
			Process proc = builder.start();
			this.log.info("Process restarted: " + proc);

			Thread.sleep(4000L);

			System.exit(-2);
		} catch (Exception e) {
			this.log.error("Restart exception: " + e);
		}
	}
}