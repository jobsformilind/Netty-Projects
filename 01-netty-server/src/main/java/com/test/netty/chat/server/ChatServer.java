package com.test.netty.chat.server;

import com.test.netty.chat.common.Constants;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ChatServer {
	private EventLoopGroup bossGroup = new NioEventLoopGroup(2);
	private EventLoopGroup workGroup = new NioEventLoopGroup(2);

	public static void main(String[] args) throws Exception {
		new ChatServer().start();
	}

	public void start() throws Exception {
		try {
			System.out.println("Starting chat server...");
			ServerBootstrap bootstrap = new ServerBootstrap()
					.group(bossGroup, workGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChatServerInitializer());
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
	
	private class ChatServerInitializer extends ChannelInitializer<SocketChannel> {
	    @Override
	    protected void initChannel(SocketChannel channel) throws Exception {
	        ChannelPipeline pipeline = channel.pipeline();
	        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
	        pipeline.addLast("decoder", new StringDecoder());
	        pipeline.addLast("encoder", new StringEncoder());
	        pipeline.addLast("handler", new ChatServerHandler());
	    }
	}
}
