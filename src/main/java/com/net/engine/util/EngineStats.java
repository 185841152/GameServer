package com.net.engine.util;

import com.net.engine.core.NetEngine;

public class EngineStats {
//	public static long getIncomingBytes() {
//		return NetEngine.getInstance().getSocketReader().getReadBytes()
//				+ NetEngine.getInstance().getDatagramReader().getReadBytes();
//	}
//
//	public static long getIncomingPackets() {
//		return NetEngine.getInstance().getSocketReader().getReadPackets();
//	}

	public static long getOutgoingBytes() {
		return NetEngine.getInstance().getSocketWriter().getWrittenBytes();
	}

	public static long getOutgoingPackets() {
		return NetEngine.getInstance().getSocketWriter().getWrittenPackets();
	}

	public static long getOutgoingDroppedPackets() {
		return NetEngine.getInstance().getSocketWriter().getDroppedPacketsCount();
	}

	public static int getRestartCount() {
		return NetEngine.getInstance().getRestartCount();
	}
}