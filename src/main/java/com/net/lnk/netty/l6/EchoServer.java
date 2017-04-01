package com.net.lnk.netty.l6;

import java.util.List;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class EchoServer {

	public static void main(String[] args) {
		int port = 8081;

		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}

		new EchoServer().bind(port);
	}

	public void bind(int port) {
		// 服务端NIO线程组
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		// 服务端辅助启动类
		ServerBootstrap b = new ServerBootstrap();

		try {
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast("msgpackEncoder", new MsgpackEncoder(Boolean.FALSE));
							ch.pipeline().addLast("msgpackDecoder", new MsgpackDecoder());
							ch.pipeline().addLast(new EchoServerHandler());
						}
					});
			// 绑定端口，同步等待成功
			ChannelFuture future = b.bind(port).sync();

			System.out.println("Time Server Started!");

			// 等待服务端监听端口关闭
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}

class EchoServerHandler extends ChannelHandlerAdapter {

	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		 @SuppressWarnings("unchecked")
		 List<UserBean> users = (List<UserBean>) msg;
		 for (UserBean user : users) {
		 System.out.println("Receive the msgpack message : " + user);
		 ctx.write(users);
		 }

//		UserBean user = (UserBean) msg;
//		System.out.println("Receive the msgpack message : " + user);
	}

	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}