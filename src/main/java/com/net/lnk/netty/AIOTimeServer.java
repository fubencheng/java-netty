package com.net.lnk.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class AIOTimeServer {

	public static void main(String[] args) {
		int port = 8081;

		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}

		AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(port);
		new Thread(timeServer, "AIO-AsyncTimeServerHandler-001").start();
	}
}

class AsyncTimeServerHandler implements Runnable {

	AsynchronousServerSocketChannel asyncChannel;
	CountDownLatch latch;

	public AsyncTimeServerHandler(int port) {
		try {
			asyncChannel = AsynchronousServerSocketChannel.open();
			asyncChannel.bind(new InetSocketAddress(port));
			System.out.println("Time server is start in port : " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		latch = new CountDownLatch(1);
		doAccept();

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void doAccept() {
		asyncChannel.accept(this, new AcceptCompletionHandler());
	}
}

class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {

	public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment) {
		// loop accept new request
		attachment.asyncChannel.accept(attachment, this);
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		result.read(buffer, buffer, new ReadCompletionHandler(result));
	}

	public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
		exc.printStackTrace();
		attachment.latch.countDown();
	}
}

class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

	private AsynchronousSocketChannel socketChannel;

	public ReadCompletionHandler(AsynchronousSocketChannel socketChannel) {
		if (this.socketChannel == null)
			this.socketChannel = socketChannel;
	}

	public void completed(Integer result, ByteBuffer buffer) {
		buffer.flip();
		byte[] body = new byte[buffer.remaining()];
		buffer.get(body);
		try {
			String req = new String(body, Charset.defaultCharset());
			System.out.println("The time server receive order : " + req);

			String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(req)
					? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
			doWrite(currentTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doWrite(String resp) {
		if (resp != null && resp.trim().length() > 0) {
			byte[] bytes = resp.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			socketChannel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {

				public void completed(Integer result, ByteBuffer buffer) {
					// 如果没有发送完成，继续发送
					if (buffer.hasRemaining()) {
						socketChannel.write(buffer, buffer, this);
					}

					System.out.println("Send response succeed!");
				}

				public void failed(Throwable exc, ByteBuffer attachment) {
					try {
						socketChannel.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void failed(Throwable exc, ByteBuffer attachment) {
		try {
			this.socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
