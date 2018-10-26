package com.net.engine.websocket;

public class WebSocketStats {
	private volatile long readBytes = 0L;
	private volatile long readPackets = 0L;
	private volatile long writtenBytes = 0L;
	private volatile long writtenPackets = 0L;
	private volatile long droppedInPackets = 0L;

	public void addDroppedInPacket() {
		this.droppedInPackets += 1L;
	}

	public long getDroppedInPackets() {
		return this.droppedInPackets;
	}

	public void addReadBytes(int value) {
		this.readBytes += value;
	}

	public void addReadPackets(int value) {
		this.readPackets += value;
	}

	public void addWrittenBytes(int value) {
		this.writtenBytes += value;
	}

	public void addWrittenPackets(int value) {
		this.writtenPackets += value;
	}

	public long getReadBytes() {
		return this.readBytes;
	}

	public long getReadPackets() {
		return this.readPackets;
	}

	public long getWrittenBytes() {
		return this.writtenBytes;
	}

	public long getWrittenPackets() {
		return this.writtenPackets;
	}
}