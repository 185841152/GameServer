package com.net.engine.io.protocols;

public class PacketHeader {
	private int expectedLen = -1;
	private final boolean binary;
	private final boolean compressed;
	private final boolean encrypted;
	private final boolean bigSized;

	public PacketHeader(boolean binary, boolean bigSized,boolean compressed,boolean encrypted) {
		this.binary = binary;
		this.compressed = compressed;
		this.encrypted = encrypted;
		this.bigSized = bigSized;
	}

	public int getExpectedLen() {
		return this.expectedLen;
	}

	public void setExpectedLen(int len) {
		this.expectedLen = len;
	}

	public boolean isBinary() {
		return this.binary;
	}

	public boolean isCompressed() {
		return this.compressed;
	}

	public boolean isEncrypted() {
		return this.encrypted;
	}

	public boolean isBigSized() {
		return this.bigSized;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();

		buf.append("\n---------------------------------------------\n");
		buf.append("Binary:  \t" + isBinary() + "\n");
		buf.append("Encrypted:\t" + isEncrypted() + "\n");
		buf.append("Compressed:\t" + isCompressed() + "\n");
		buf.append("BigSized:\t" + isBigSized() + "\n");
		buf.append("---------------------------------------------\n");

		return buf.toString();
	}
}