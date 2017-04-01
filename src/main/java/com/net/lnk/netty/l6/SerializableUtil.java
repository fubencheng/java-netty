package com.net.lnk.netty.l6;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class SerializableUtil {

	public static void main(String[] args) throws Exception {
		UserInfo user = new UserInfo("Welcome to Netty", 100);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(user);
		out.flush();
		out.close();
		byte[] b = baos.toByteArray();
		System.out.println("The jdk serializable length is : " + b.length);
		baos.close();

		System.out.println("------------------------");
		ByteBuffer buffer = ByteBuffer.allocate(1024);

		System.out.println("The byte array serializable length is : " + user.codeC(buffer).length);

		System.out.println("\n *********************************** \n ");

		int loop = 1000000;

		ByteArrayOutputStream baStream = null;
		ObjectOutputStream oStream = null;
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < loop; i++) {
			baStream = new ByteArrayOutputStream();
			oStream = new ObjectOutputStream(baStream);
			oStream.writeObject(user);
			oStream.flush();
			oStream.close();
			baStream.toByteArray();
			baStream.close();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("The jdk serializable cost time is : " + (endTime - startTime) + " ms");

		startTime = System.currentTimeMillis();
		for (int i = 0; i < loop; i++) {
			user.codeC(buffer);
		}
		endTime = System.currentTimeMillis();

		System.out.println("The byte array serializable cost time is : " + (endTime - startTime) + " ms");
	}

}

class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String userName;
	private int userId;

	public UserInfo(String userName, int userId) {
		this.setUserName(userName);
		this.setUserId(userId);
	}

	public byte[] codeC(ByteBuffer buffer) {
		buffer.clear();
		buffer.putInt(this.getUserName().getBytes().length);
		buffer.put(this.getUserName().getBytes());
		buffer.putInt(this.getUserId());
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
}