package com.test.netty.hello.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class HelloNettyServerHandler extends ChannelInboundHandlerAdapter {
	private static int counter = 0;

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) {
		System.out.println("Established connection: ");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("Server received: ");
		ByteBuf inBuffer = (ByteBuf) msg;
		String received = inBuffer.toString(CharsetUtil.UTF_8);
		System.out.println("Server received: " + received);
		Thread.sleep(5000);
		ctx.writeAndFlush(Unpooled.copiedBuffer("Hello  venu" + received, CharsetUtil.UTF_8));
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println(" channel read complete ");
		ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		decrement();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	public void channelActive(ChannelHandlerContext ctx) throws java.lang.Exception {
		increment();
		System.out.println(" channel activated counter is " + getCount());
	}

	private synchronized int getCount() {
		return counter;
	}

	private synchronized void increment() {
		counter++;
	}

	private synchronized void decrement() {
		counter--;
	}
}