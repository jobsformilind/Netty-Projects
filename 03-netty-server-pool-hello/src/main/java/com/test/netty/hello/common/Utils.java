package com.test.netty.hello.common;

public class Utils {
	public static void print(String message) {
		System.out.println(Thread.currentThread().getName() + ": " + message);
	}
}
