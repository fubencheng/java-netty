package com.net.lnk.netty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @tag one request need one thread handle
 * @memo 2016年11月21日
 */
public class BIOTimeServer {

	public static void main(String[] args) {
		int port = 8081;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// do nothing, use default
			}
		}

		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("Time server is start in port : " + port);
			Socket socket = null;
			while (true) {
				socket = server.accept();
				new Thread(new TimeServerHandle(socket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					server.close();
					System.out.println("Time server is closed");
					server = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

class TimeServerHandle implements Runnable {
	private Socket socket;

	public TimeServerHandle(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream(), true);
			String msg;
			String currentTime;

			try {
				TimeUnit.SECONDS.sleep(5L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			while ((msg = br.readLine()) != null) {
				System.out.println("Time server received message : " + msg);
				currentTime = "QUERY TIME ORDER".equalsIgnoreCase(msg) ? new Date(System.currentTimeMillis()).toString()
						: "BAD ORDER";
				pw.println(currentTime);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
					System.out.println("Socket is closed");
				} catch (IOException e) {
					e.printStackTrace();
				}
				socket = null;
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				br = null;
			}
			if (pw != null) {
				pw.close();
				pw = null;
			}
		}
	}
}
