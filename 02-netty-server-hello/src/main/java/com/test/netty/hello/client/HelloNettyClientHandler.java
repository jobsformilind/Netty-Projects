package com.test.netty.hello.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class HelloNettyClientHandler extends SimpleChannelInboundHandler<Object> {
	final BlockingQueue<String> answer = new LinkedBlockingQueue<String>();

	public String getFactorial() {
		boolean interrupted = false;
		try {
			for (;;) {
				try {
					return answer.take();
				} catch (InterruptedException ignore) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public HelloNettyClientHandler() {
		System.out.println("INstantiated client handler");
	}

	@Override
	public void channelActive(ChannelHandlerContext channelHandlerContext) {
		System.out.println("Cient channel active called  ");
		channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer("Netty Rocks!", CharsetUtil.UTF_8));
	}

	public void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf in) {
		System.out.println("Client received: " + in.toString(CharsetUtil.UTF_8));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
		cause.printStackTrace();
		channelHandlerContext.close();
	}

	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
		ByteBuf inBuffer = (ByteBuf) o;
		String received = inBuffer.toString(CharsetUtil.UTF_8);
		System.out.println("Clinet channel read 0 received: " + received);
		answer.add(received);
	}
}