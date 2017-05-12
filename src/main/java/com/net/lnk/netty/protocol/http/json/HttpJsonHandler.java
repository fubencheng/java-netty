package com.net.lnk.netty.protocol.http.json;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HttpJsonHandler extends SimpleChannelInboundHandler<Object> {

	protected void messageReceived(ChannelHandlerContext arg0, Object msg) throws Exception {
		System.out.println(msg);

	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.channel().close();
	}

	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		messageReceived(ctx, msg);
	}

}
