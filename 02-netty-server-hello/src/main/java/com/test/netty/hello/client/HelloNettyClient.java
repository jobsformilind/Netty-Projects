package com.test.netty.hello.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

public class HelloNettyClient {
	private static boolean initialized = false;

	public HelloNettyClient() {
		System.out.println("Called netty client");
	}

	private static FixedChannelPool fcp;

	public static void initialize() {
		if (!initialized) {
			EventLoopGroup group = new NioEventLoopGroup();
			final Bootstrap cb = new Bootstrap();
			cb.handler(new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					socketChannel.pipeline().addLast(new HelloNettyClientHandler());
				}
			});
			cb.remoteAddress("localhost", 9999);
			cb.group(group).channel(NioSocketChannel.class);
			fcp = new FixedChannelPool(cb, new HelloClientChannelPoolHandler(), 2, 30000);
			initialized = true;
		} else {
			System.out.println("already initialized");
		}
	}

	public static void getClientHandler() {
		initialize();
		try {
			Future<Channel> f = fcp.acquire();
			Channel c = f.get();
			HelloNettyClientHandler ch = (HelloNettyClientHandler) c.pipeline().last();
			System.out.println("Fetched " + ch.getFactorial());
			fcp.release(c);
		} catch (Exception e) {
		}
	}
}