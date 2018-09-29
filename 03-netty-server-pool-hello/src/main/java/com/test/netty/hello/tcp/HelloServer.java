package com.test.netty.hello.tcp;

import com.test.netty.hello.common.Constants;
import com.test.netty.hello.common.Utils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

public class HelloServer {
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	public void start() {
		Utils.print("Starting HelloServer...");
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup(2);
		try {
			final ServerBootstrap b = new ServerBootstrap();
			b.option(ChannelOption.SO_BACKLOG, 1024);
			b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			b.childOption(ChannelOption.SO_KEEPALIVE, true);
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
			b.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new HelloServerChannelInitializer());
			Utils.print("HelloServer started..");
			b.bind(Constants.SERVER_HOST, Constants.SERVER_PORT).sync().channel().closeFuture().sync();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			stop();
		}
	}

	public void stop() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	public static void main(final String[] args) {
		final HelloServer server = new HelloServer();
		server.start();
		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
	}
}

@ChannelHandler.Sharable
class HelloServerHandler extends SimpleChannelInboundHandler<String> {

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final String msg) {
		ctx.write("Hello!\n");
	}

	@Override
	public void channelReadComplete(final ChannelHandlerContext ctx) {
		ctx.flush();
	}
}

class HelloServerChannelInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		Utils.print("Initializing HelloServer channel : " + ch);
		final ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
		pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
		pipeline.addLast("serverHandler", new HelloServerHandler());
		Utils.print("HelloServer channel initialized: " + ch);
	}
}
