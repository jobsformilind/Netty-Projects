package com.test.netty.chat.client;

import java.util.Scanner;

import com.test.netty.chat.common.Constants;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ChatClient {
	private EventLoopGroup group = new NioEventLoopGroup(2);
	private Scanner scanner;

	public static void main(String[] args) throws Exception {
		new ChatClient().start();
	}

	public void start() throws Exception {
		try {
			System.out.println("Starting Chat Client...");
			Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
					.handler(new ChatClientInitializer());
			Channel channel = bootstrap.connect(Constants.SERVER_HOST, Constants.SERVER_PORT).sync().channel();
			System.out.println("Chat Client started.");

			scanner = new Scanner(System.in);
			while (true) {
				channel.write(scanner.nextLine() + "\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			stop();
		}
	}

	public void stop() {
		scanner.close();
		group.shutdownGracefully();
		System.out.println("Chat Client stopped.");
	}

}
