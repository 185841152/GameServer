package com.net.engine.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class DefaultPacketCompressor{
	public final static int MAX_SIZE_FOR_COMPRESSION = 1000000;
	private final static int compressionBufferSize = 512;

	public static byte[] compress(byte[] data) throws Exception {
		if (data.length > 1000000) {
			return data;
		}

		Deflater compressor = new Deflater();

		compressor.setInput(data);
		compressor.finish();

		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

		byte[] buf = new byte[compressionBufferSize];
		while (!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}

		bos.close();

		return bos.toByteArray();
	}

	public static byte[] uncompress(byte[] zipData) throws Exception {
		Inflater unzipper = new Inflater();
		unzipper.setInput(zipData);

		ByteArrayOutputStream bos = new ByteArrayOutputStream(zipData.length);

		byte[] buf = new byte[compressionBufferSize];
		try {
			while (!unzipper.finished()) {
				int count = unzipper.inflate(buf);

				if ((count < 1) && (unzipper.needsInput())) {
					throw new IOException("Bad Compression Format! Packet will be dropped");
				}
				bos.write(buf, 0, count);
			}

			byte[] arrayOfByte1 = bos.toByteArray();
			return arrayOfByte1;
		} finally {
			bos.close();
		}
	}
}