package com.test.netty.chat.server;

import java.net.InetAddress;
import java.util.Date;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {
	private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	protected void channelRead0(ChannelHandlerContext context, String msg) throws Exception {
		System.out.println("Message received: " + msg);
		if ("bye".equals(msg)) {
			context.close();
		}
		Date date = new Date();
		context.writeAndFlush(date + "\n");
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Channel Active: " + ctx.channel().remoteAddress());
		ctx.writeAndFlush("SERVER: " + InetAddress.getLocalHost().getHostName() + "How are you? \n");
		super.channelActive(ctx);
	}

	public void channelRead1(ChannelHandlerContext ctx, String msg) throws Exception {
		Channel incoming = ctx.channel();
		for (Channel channel : channels) {
			if (channel != incoming) {
				channel.write("[" + incoming.remoteAddress() + "]" + msg + "\n");
			}
		}
	}

	public void handlerAdded1(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		for (Channel channel : channels) {
			channel.write("[SERVER] - " + incoming.remoteAddress() + " has joined\n");
		}
		channels.add(ctx.channel());
	}

	public void handlerRemoved1(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		for (Channel channel : channels) {
			channel.write("[SERVER] - " + incoming.remoteAddress() + " has left\n");
		}
		channels.remove(ctx.channel());
	}

}
