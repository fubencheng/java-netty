package com.net.lnk.netty.l4;

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
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class LineBasedFrameDecoderTimeClient {

	public static void main(String[] args) {
		int port = 8081;

		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}

		new LineBasedFrameDecoderTimeClient().connect("127.0.0.1", port);
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
							ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
							ch.pipeline().addLast(new StringDecoder(Charset.defaultCharset()));
							ch.pipeline().addLast(new LineBasedFrameDecoderTimeClientHandler());
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

class LineBasedFrameDecoderTimeClientHandler extends ChannelHandlerAdapter {

	private byte[] req;
	private int counter;

	public LineBasedFrameDecoderTimeClientHandler() {
		req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();
	}

	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ByteBuf msg = null;
		for (int i = 0; i < 100; i++) {
			msg = Unpooled.buffer(req.length);
			msg.writeBytes(req);
			ctx.writeAndFlush(msg);
		}
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String body = (String) msg;
		System.out.println("Now is : " + body + ", the counter is : " + ++counter);
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// 释放资源
		ctx.close();
	}

}
