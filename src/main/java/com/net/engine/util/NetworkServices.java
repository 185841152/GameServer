package com.net.engine.util;

import java.nio.ByteBuffer;

import com.net.engine.data.BufferType;

public class NetworkServices {
	public static ByteBuffer allocateBuffer(int size, BufferType type) {
		ByteBuffer bb = null;

		if (type == BufferType.DIRECT) {
			bb = ByteBuffer.allocateDirect(size);
		} else if (type == BufferType.HEAP) {
			bb = ByteBuffer.allocate(size);
		}
		return bb;
	}
}