package com.net.lnk.netty.codec.messagepack;

import java.util.List;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MsgpackDecoder extends MessageToMessageDecoder<ByteBuf> {

	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, Object object) throws Exception {
		final int length = buffer.readableBytes();
		final byte[] bytes = new byte[length];
		buffer.getBytes(buffer.readerIndex(), bytes, 0, length);
		MessagePack pack = new MessagePack();
		pack.read(bytes);
	}

	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> list) throws Exception {
		final int length = buffer.readableBytes();
		final byte[] bytes = new byte[length];
		buffer.getBytes(buffer.readerIndex(), bytes, 0, length);
		MessagePack pack = new MessagePack();
		list.add(pack.read(bytes));
	}

}
