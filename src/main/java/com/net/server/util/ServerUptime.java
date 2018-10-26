package com.net.server.util;

import java.text.DecimalFormat;
import java.text.Format;

public class ServerUptime {
	private static final int ONE_DAY = 86400000;
	private static final int ONE_HOUR = 3600000;
	private static final int ONE_MINUTE = 60000;
	private static final int ONE_SECOND = 1000;
	private int days;
	private int hours;
	private int minutes;
	private int seconds;

	public ServerUptime(long unixTime) {
		this.days = (int) Math.floor(unixTime / ONE_DAY);
		unixTime -= 86400000L * this.days;

		this.hours = (int) Math.floor(unixTime / ONE_HOUR);
		unixTime -= 3600000 * this.hours;

		this.minutes = (int) Math.floor(unixTime / ONE_MINUTE);
		unixTime -= 60000 * this.minutes;

		this.seconds = (int) Math.floor(unixTime / ONE_SECOND);
	}

	public int getDays() {
		return this.days;
	}

	public int getHours() {
		return this.hours;
	}

	public int getMinutes() {
		return this.minutes;
	}

	public int getSeconds() {
		return this.seconds;
	}

	public String toString() {
		Format fmt = new DecimalFormat("##00");

		return String.format("%s days, %s:%s:%s",
				new Object[] { Integer.valueOf(this.days), fmt.format(Integer.valueOf(this.hours)),
						fmt.format(Integer.valueOf(this.minutes)), fmt.format(Integer.valueOf(this.seconds)) });
	}
}