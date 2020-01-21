package com.test.netty.hello.client.test;

import java.util.concurrent.Callable;
import com.test.netty.hello.client.HelloNettyClient;

public class CallableClient implements Callable<Object> {
	public Object call() {
		HelloNettyClient.getClientHandler();
		return null;
	}
}