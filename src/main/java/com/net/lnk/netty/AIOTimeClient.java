package com.net.lnk.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

public class AIOTimeClient {

	public static void main(String[] args) {
		int port = 8081;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}

		new Thread(new AsyncTimeClientHandler("127.0.0.1", port), "AIO-AsyncTimeClientHandler-001").start();

	}

}

class AsyncTimeClientHandler implements CompletionHandler<Void, AsyncTimeClientHandler>, Runnable {
	private String host;
	private int port;
	private AsynchronousSocketChannel socketChannel;
	private CountDownLatch latch;

	public AsyncTimeClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			socketChannel = AsynchronousSocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void run() {
		latch = new CountDownLatch(1);
		socketChannel.connect(new InetSocketAddress(host, port), this, this);
		System.out.println("Connect to host : " + host + ", port : " + port);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void completed(Void result, AsyncTimeClientHandler attachment) {
		byte[] req = "QUERY TIME ORDER".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		socketChannel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {

			public void completed(Integer result, ByteBuffer buffer) {
				if (buffer.hasRemaining()) {
					socketChannel.write(buffer, buffer, this);
				} else {
					ByteBuffer readBuffer = ByteBuffer.allocate(1024);
					socketChannel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {

						public void completed(Integer result, ByteBuffer buffer) {
							buffer.flip();
							byte[] bytes = new byte[buffer.remaining()];
							buffer.get(bytes);
							try {
								String resp = new String(bytes, Charset.defaultCharset());
								System.out.println("Now is : " + resp);
								latch.countDown();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						public void failed(Throwable exc, ByteBuffer buffer) {
							try {
								socketChannel.close();
								latch.countDown();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}

			public void failed(Throwable exc, ByteBuffer buffer) {
				try {
					socketChannel.close();
					latch.countDown();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void failed(Throwable exc, AsyncTimeClientHandler clientHandler) {
		exc.printStackTrace();
		try {
			socketChannel.close();
			latch.countDown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
