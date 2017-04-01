package com.net.lnk.netty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BIOTimeClient {

	public static void main(String[] args) {
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			socket = new Socket("127.0.0.1", 8081);
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println("QUERY TIME ORDER~");
			System.out.println("Send order 2 server succeed");

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String resp = in.readLine();
			System.out.println("Now is : " + resp);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				socket = null;
			}
			if (out != null) {
				out.close();
				out = null;
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				in = null;
			}
		}
	}
}
