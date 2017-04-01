package com.net.lnk.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class NIOTimeServer {

	public static void main(String[] args) {
		int port = 8081;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// do nothing, use default
			}
		}

		MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
		new Thread(timeServer, "MultiplexerTimeServer-001").start();
	}
}

class MultiplexerTimeServer implements Runnable {

	private ServerSocketChannel svrChannel;
	private Selector selector;
	private volatile boolean stop;

	public MultiplexerTimeServer(int port) {
		try {
			svrChannel = ServerSocketChannel.open();
			selector = Selector.open();
			svrChannel.configureBlocking(false);
			svrChannel.socket().bind(new InetSocketAddress(port), 1024);
			svrChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("The time server is start in port : " + port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void run() {
		while (!stop) {
			try {
				selector.select(1000L);
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();
				SelectionKey key = null;
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if (key != null) {
							key.cancel();
						}
						if (key.channel() != null) {
							key.channel().close();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			// deal with new acceptable request
			if (key.isAcceptable()) {
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				sc.register(selector, SelectionKey.OP_READ);
			}
			if (key.isReadable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				while ((sc.read(buffer)) > 0) {
					buffer.flip();
					byte[] bytes = new byte[buffer.remaining()];
					buffer.get(bytes);
					String body = new String(bytes, Charset.defaultCharset());
					System.out.println("Time server received order : " + body);

					String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
							? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
					doWrite(sc, currentTime);
				}

				key.cancel();
				sc.close();
			}
		}
	}

	private void doWrite(SocketChannel sc, String resp) throws IOException {
		if (resp != null && resp.length() > 0) {
			byte[] bytes = resp.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			sc.write(writeBuffer);
			if (!writeBuffer.hasRemaining()) {
				System.out.println("Response 2 client succeed!");
			}
		}
	}

}
