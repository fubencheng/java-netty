package com.net.lnk.netty.l6;

import java.io.Serializable;

import org.msgpack.annotation.Message;

/**
 * 要传输的javabean一定要加上注解@Message
 */
@Message
class UserBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int age;

	public UserBean() {

	}

	// *如果显示定义了构造函数，一定要显示定义无参构造函数
	public UserBean(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String toString() {
		return "User : age = " + age + ", name = " + name;
	}

}
