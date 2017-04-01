package com.net.lnk.netty.l6;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgpackEncoder extends MessageToByteEncoder<Object> {
	private static final MessagePack pack = new MessagePack();

	private boolean required = Boolean.FALSE;

	public MsgpackEncoder(boolean required) {
		this.required = required;
	}

	protected void encode(ChannelHandlerContext ctx, Object object, ByteBuf buffer) throws Exception {

		if (object == null) {
			if (required) {
				throw new MessageTypeException("Attempted to write null");
			}
			return;
		}

		byte[] raw = pack.write(object);
		buffer.writeBytes(raw);
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

}
