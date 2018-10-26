package com.net.engine.io.protocols;

import java.util.List;

import com.net.engine.data.IPacket;
import com.net.engine.data.Packet;
import com.net.engine.data.TransportType;
import com.net.engine.util.DefaultIoHandler;
import com.net.engine.util.DefaultPacketCompressor;
import com.net.server.data.DefaultGameDataSerializer;
import com.net.server.data.IGameObject;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 消息解码器
 * @author sunjian
 *
 */
public class MessageDecoder extends ByteToMessageDecoder {
	private enum State {
		Header, Body
	}
	private State state = State.Header;
	private int totalSize;
	private boolean compressed;
	private boolean binary;

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
		switch (state) {
		case Header:
			// 如果可读消息小于1，表示没有消息头
			if (in.readableBytes() < 5) {
				return;
			}
			// 消息头
			byte header = in.readByte();
			// 解析消息头
			PacketHeader packetHeader = DefaultIoHandler.decodeFirstHeaderByte(header);
			if (!packetHeader.isBigSized()) {
				totalSize = in.readShort();
			} else {
				totalSize = in.readInt();
			}
			// 消息是否压缩
			compressed = packetHeader.isCompressed();
			binary = packetHeader.isBinary();
			state = State.Body;
			break;
		case Body:
			// 消息长度不够，返回
			if (in.readableBytes() < totalSize) {
				return;
			}
			byte[] bs = new byte[totalSize];
			if (in.hasArray()) {
				bs = in.array();
			} else {
				try {
					in.readBytes(bs, 0, totalSize);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			// 如果压缩过，就要解压
			if (compressed) {
				bs = DefaultPacketCompressor.uncompress(bs);
			}
			// 消息解码成传输对象
			IPacket newPacket = new Packet();
			newPacket.setTransportType(TransportType.TCP);
			//消息内容
			IGameObject object=null;
			// 协议类型
			if (binary) {// 二进制协议
				object= DefaultGameDataSerializer.getInstance().binary2object(bs);
				newPacket.setData(object);
				newPacket.setOriginalSize(object.size());
				newPacket.setProtocolType(ProtocolType.BINARY);
			} else {// 文本协议
				String data = new String(bs, "utf-8");
				// 消息解码成传输对象
				object = DefaultGameDataSerializer.getInstance().json2object(data);
				newPacket.setData(object);
				newPacket.setOriginalSize(object.size());
				newPacket.setProtocolType(ProtocolType.TEXT);
			}
			out.add(newPacket);
			// 将解码指针指向消息头
			state = State.Header;
			break;
		default:
			break;
		}
	}

}
