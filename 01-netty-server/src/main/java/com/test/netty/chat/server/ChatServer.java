package com.test.netty.chat.server;

import com.test.netty.chat.common.Constants;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ChatServer {
	private EventLoopGroup bossGroup = new NioEventLoopGroup(2);
	private EventLoopGroup workGroup = new NioEventLoopGroup(2);

	public static void main(String[] args) throws Exception {
		new ChatServer().start();
	}

	public void start() throws Exception {
		try {
			System.out.println("Starting chat server...");
			ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workGroup)
					.channel(NioServerSocketChannel.class).childHandler(new ChatServerInitializer());
			ChannelFuture future = bootstrap.bind(Constants.SERVER_PORT).sync();
			System.out.println("Chat server started.");
			future.channel().closeFuture().sync();
		} finally {
			stop();
		}
	}
	
	public void stop() {
		bossGroup.shutdownGracefully();
		workGroup.shutdownGracefully();
		System.out.println("Chat server is shut down.");
	}
}
