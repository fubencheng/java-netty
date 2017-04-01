package com.net.lnk.netty.l6;

import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MsgpackDecoder extends MessageToMessageDecoder<ByteBuf> {

	@SuppressWarnings("resource")
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> list) throws Exception {
		final int length = buffer.readableBytes();
		final byte[] bytes = new byte[length];
		buffer.getBytes(buffer.readerIndex(), bytes, 0, length);
		MessagePack pack = new MessagePack();
		pack.register(UserBean.class);
		Value value = pack.read(bytes);
		UserBean user = new Converter(value).read(UserBean.class);
		list.add(user);
	}

}
