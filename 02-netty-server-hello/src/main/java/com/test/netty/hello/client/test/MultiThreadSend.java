package com.test.netty.hello.client.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadSend {
	public static void main(String[] args) {
		ExecutorService es = Executors.newFixedThreadPool(20);
		for (int i = 0; i < 2000; i++) {
			es.submit(new CallableClient());
		}
		es.shutdown();
	}
}