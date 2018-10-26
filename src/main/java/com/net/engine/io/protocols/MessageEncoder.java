package com.net.engine.io.protocols;

import com.net.engine.data.IPacket;
import com.net.engine.util.DefaultIoHandler;
import com.net.engine.util.DefaultPacketCompressor;
import com.net.server.data.DefaultGameDataSerializer;
import com.net.server.data.GameObject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<IPacket> {
	private static final int SHORT_SIZE = 2;
	private static final int INT_SIZE = 4;
	private static final int COMPRES_SIZE=1024;

	@Override
	protected void encode(ChannelHandlerContext content, IPacket packet, ByteBuf out) throws Exception {
		GameObject msg = (GameObject) packet.getData();

		// 是否是二进制协议
		boolean binary = packet.isBinary();
		byte[] bs = null;
		if (binary) {
			// 将要发送的内容转为byte
			bs = DefaultGameDataSerializer.getInstance().object2binary(msg);
		} else {
			bs = msg.toJson().getBytes("utf-8");
		}
		// 是否加密
		boolean isEncrypted = false;
		// 是否压缩
		boolean isCompressed = false;

		// 原内容长度
		int originalSize = bs.length;

		if (!isEncrypted) {
			// 消息大于压缩阀值，就压缩
			if (bs.length > COMPRES_SIZE) {
				byte[] beforeCompression = bs;
				bs = DefaultPacketCompressor.compress(bs);

				if (bs != beforeCompression) {
					isCompressed = true;
				}
			}
		}

		int sizeBytes = SHORT_SIZE;
		// 如果消息大于两个字节
		if (bs.length > 65535) {
			sizeBytes = INT_SIZE;
		}
		// 消息头
		PacketHeader packetHeader = new PacketHeader(true, sizeBytes > 2, isCompressed, isEncrypted);
		// 将消息头转化为byte
		byte headerByte = DefaultIoHandler.encodeFirstHeaderByte(packetHeader);
		// 写入消息头
		out.writeByte(headerByte);
		// 消息长度
		if (sizeBytes > SHORT_SIZE)
			out.writeInt(bs.length);
		else {
			out.writeShort((short) bs.length);
		}
		// 写入消息内容
		out.writeBytes(bs);
	}
}
