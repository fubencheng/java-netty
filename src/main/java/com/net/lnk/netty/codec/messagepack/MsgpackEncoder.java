package com.net.lnk.netty.codec.messagepack;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgpackEncoder extends MessageToByteEncoder<Object> {
	private static final MessagePack pack = new MessagePack();

	public MsgpackEncoder() {
	}

	protected void encode(ChannelHandlerContext ctx, Object object, ByteBuf buffer) throws Exception {

		if (object == null) {
			throw new MessageTypeException("Attempted to write null");
		}

		byte[] raw = pack.write(object);
		buffer.writeBytes(raw);
	}

}
