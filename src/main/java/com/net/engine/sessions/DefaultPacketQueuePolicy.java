package com.net.engine.sessions;

import com.net.engine.data.IPacket;
import com.net.engine.data.MessagePriority;
import com.net.engine.exceptions.PacketQueueWarning;

public class DefaultPacketQueuePolicy implements IPacketQueuePolicy {
	private static final int THREE_QUARTERS_FULL = 75;
	private static final int NINETY_PERCENT_FULL = 90;

	public void applyPolicy(IPacketQueue packetQueue, IPacket packet) throws PacketQueueWarning {
		int percentageFree = packetQueue.getSize() * 100 / packetQueue.getMaxSize();

		if ((percentageFree >= THREE_QUARTERS_FULL) && (percentageFree < NINETY_PERCENT_FULL)) {
			if (packet.getPriority().getValue() < MessagePriority.NORMAL.getValue()) {
				fireDropMessageError(packet, percentageFree);
			}
		} else if (percentageFree >= NINETY_PERCENT_FULL) {
			if (packet.getPriority().getValue() < MessagePriority.HIGH.getValue())
				fireDropMessageError(packet, percentageFree);
		}
	}

	private void fireDropMessageError(IPacket packet, int percentageFree) {
		throw new PacketQueueWarning("Dropping packet: " + packet + ", Free queue: " + percentageFree + "%");
	}
	
}