package com.net.engine.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.slf4j.Logger;


public class Logging {
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final String TAB = "\t";

	public static void changeConsoleHandlerLevel(Level newLevel) {
		java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();

		if (handlers.length > 0) {
			Handler firstHandler = handlers[0];
			if ((firstHandler != null) && ((firstHandler instanceof ConsoleHandler)))
				firstHandler.setLevel(newLevel);
		}
	}

	public static Level getConsoleHandlerLevel() {
		Level level = null;

		java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();

		if (handlers.length > 0) {
			Handler firstHandler = handlers[0];
			if ((firstHandler != null) && ((firstHandler instanceof ConsoleHandler))) {
				level = firstHandler.getLevel();
			}
		}
		return level;
	}

	public static void changeConsoleHandlerFormatter(Formatter formatter) {
		java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();

		if (handlers.length > 0) {
			Handler firstHandler = handlers[0];
			if ((firstHandler != null) && ((firstHandler instanceof ConsoleHandler))) {
				firstHandler.setFormatter(formatter);
			} else
				throw new RuntimeException("Could not change the ConsoleHandler's formatter!");
		}
	}

	public static void logStackTrace(Logger logger, Throwable throwable) {
		logStackTrace(logger, throwable.toString(), throwable.getStackTrace());
	}

	public static void logStackTrace(Logger logger, StackTraceElement[] stackTrace) {
		logStackTrace(logger, null, stackTrace);
	}

	public static void logStackTrace(Logger logger, String cause, StackTraceElement[] stackTrace) {
		StringBuilder sb = new StringBuilder();

		if (cause != null) {
			sb.append(cause).append(NEW_LINE);
		}
		for (StackTraceElement element : stackTrace) {
			sb.append(TAB).append(element).append(NEW_LINE);
		}

		logger.warn(sb.toString());
	}

	public static String formatStackTrace(StackTraceElement[] elements) {
		StringBuilder sb = new StringBuilder();

		StackTraceElement[] arrayOfStackTraceElement = elements;
		int j = elements.length;
		for (int i = 0; i < j; i++) {
			StackTraceElement element = arrayOfStackTraceElement[i];

			sb.append(element).append(NEW_LINE);
		}

		return sb.toString();
	}
}