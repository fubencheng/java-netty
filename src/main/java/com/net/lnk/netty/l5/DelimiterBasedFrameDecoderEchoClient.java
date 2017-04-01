package com.net.lnk.netty.l5;

import java.nio.charset.Charset;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class DelimiterBasedFrameDecoderEchoClient {

	public static void main(String[] args) {
		int port = 8081;

		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}

		new DelimiterBasedFrameDecoderEchoClient().connect("127.0.0.1", port);
	}

	public void connect(String host, int port) {
		// 配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();
		// 客户端辅助启动类
		Bootstrap b = new Bootstrap();

		try {
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(
									new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("$_".getBytes())));
							ch.pipeline().addLast(new StringDecoder(Charset.defaultCharset()));
							ch.pipeline().addLast(new DelimiterBasedFrameDecoderEchoClientHandler());
						}
					});
			// 发起异步连接操作
			ChannelFuture future = b.connect(host, port).sync();

			System.out.println("Connection established!");

			// 等待客户端链路关闭
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 优雅退出，释放线程池资源
			group.shutdownGracefully();
		}
	}
}

class DelimiterBasedFrameDecoderEchoClientHandler extends ChannelHandlerAdapter {

	private static final String REQ = "Hi, welcome to Netty $_";
	private int counter;

	public DelimiterBasedFrameDecoderEchoClientHandler() {
	}

	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ByteBuf msg = null;
		for (int i = 0; i < 100; i++) {
			msg = Unpooled.copiedBuffer(REQ.getBytes());
			ctx.writeAndFlush(msg);
		}
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String body = (String) msg;
		System.out.println("This is " + ++counter + " times receive server :[" + body + "]");
	}

	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// 释放资源
		ctx.close();
	}

}
