package com.net.engine.util;

import com.net.engine.io.protocols.PacketHeader;

public class DefaultIoHandler {

	public static PacketHeader decodeFirstHeaderByte(byte headerByte) {
		return new PacketHeader(
				(headerByte & 2) > 0,
				(headerByte & 4) > 0, 
				(headerByte & 8) > 0,
				(headerByte & 16) > 0);
	}

	public static byte encodeFirstHeaderByte(PacketHeader packetHeader) {
		byte headerByte = 0;

		if (packetHeader.isEncrypted()) {
			headerByte = (byte) (headerByte + 16);
		}
		if (packetHeader.isCompressed()) {
			headerByte = (byte) (headerByte + 8);
		}
		if (packetHeader.isBigSized()) {
			headerByte = (byte) (headerByte + 4);
		}
		if (packetHeader.isBinary()) {
			headerByte = (byte) (headerByte + 2);
		}
		return headerByte;
	}

}