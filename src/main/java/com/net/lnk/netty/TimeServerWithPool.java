package com.net.lnk.netty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @tag use thread pool deal with request
 * @memo 2016年11月21日
 */
public class TimeServerWithPool {

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
		// create IO task thread pool
		// TimeServerHandlerExecutorPool singleExecutor = new
		// TimeServerHandlerExecutorPool(50, 1000);
		NewTimeServerHandlerExecutorPool singleExecutor = new NewTimeServerHandlerExecutorPool(50, 1000);
		try {
			server = new ServerSocket(port);
			System.out.println("Time server is start in port : " + port);
			Socket socket = null;
			while (true) {
				socket = server.accept();
				singleExecutor.execute(new TimeServerHandler(socket));
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

class TimeServerHandlerExecutorPool {
	private ExecutorService executorService;

	public TimeServerHandlerExecutorPool(int maxPoolSize, int queueSize) {
		executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120L,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize));
	}

	public void execute(Runnable task) {
		executorService.execute(task);
	}

}

class NewTimeServerHandlerExecutorPool extends ThreadPoolExecutor {

	public NewTimeServerHandlerExecutorPool(int maximumPoolSize, int queueSize) {
		super(Runtime.getRuntime().availableProcessors(), maximumPoolSize, 120L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>());
	}

}

class TimeServerHandler implements Runnable {
	private Socket socket;

	public TimeServerHandler(Socket socket) {
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
