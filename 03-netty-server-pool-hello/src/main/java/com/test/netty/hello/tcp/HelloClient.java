package com.test.netty.hello.tcp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.test.netty.hello.common.Constants;
import com.test.netty.hello.common.Utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

public class HelloClient {
	public static final AttributeKey<CompletableFuture<String>> FUTURE = AttributeKey.valueOf("future");
	public static final AttributeKey<ChannelPool> FUTURE_POOL = AttributeKey.valueOf("channel_pool");
	private ChannelPool channelPool;
	private EventLoopGroup eventLoopGroup;

	private void start() {
		Utils.print("Starting client");
		eventLoopGroup = new NioEventLoopGroup(3);
		Bootstrap b = new Bootstrap();
		b.option(ChannelOption.SO_KEEPALIVE, true);
		b.group(eventLoopGroup).channel(NioSocketChannel.class).remoteAddress(Constants.SERVER_HOST, Constants.SERVER_PORT);
		channelPool = new FixedChannelPool(b, new HelloCLientChannelPoolHandler(channelPool), 5);
		Utils.print("Channel pool created: " + channelPool);
	}

	private void disconnect() throws Exception {
		eventLoopGroup.shutdownGracefully().sync();
		Utils.print("Client shut down completed.");
	}

	public CompletableFuture<String> send(final String message) {
		CompletableFuture<String> future = new CompletableFuture<>();

		Future<Channel> channelFuture = channelPool.acquire();
		channelFuture.addListener(new FutureListener<Channel>() {
			@Override
			public void operationComplete(Future<Channel> f) {
				if (f.isSuccess()) {
					Channel channel = f.getNow();
					channel.attr(HelloClient.FUTURE).set(future);
					channel.attr(HelloClient.FUTURE_POOL).set(channelPool);					
					channel.writeAndFlush(message);
				}
			}
		});
		return future;
	}

	public static void main(String[] args) throws Exception {

		HelloClient client = new HelloClient();
		final int messages = Integer.valueOf(System.getProperty("messages", "100000"));
		final int iterations = Integer.valueOf(System.getProperty("iterations", "1"));
		client.start();

		long start1 = System.currentTimeMillis();
		IntStream.range(0, iterations).forEach(iteration -> {
			System.out.println("---------------------Iteration ->" + iteration + "---------------------");
			System.out.println("Asynchronously sending " + (messages / 1000) + "K messages.");
			CompletableFuture[] futures = new CompletableFuture[messages];

			long start = System.currentTimeMillis();

			IntStream.range(0, messages).forEach(index -> {
				try {
					CompletableFuture<String> future = client.send("Hello from client\n");
					futures[index] = future;
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			System.out.println("It took " + (System.currentTimeMillis() - start) + "ms to asynchronously send "
					+ (messages / 1000) + "K messages.");

			System.out.println("Waiting to get responses from server.");
			CompletableFuture f = CompletableFuture.allOf(futures);
			try {
				f.get(60, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println(
					"Server handles " + (messages / (System.currentTimeMillis() - start)) + "K messages per second");
		});
		System.out.println("Final time taken : " + (System.currentTimeMillis() - start1) + " ms for messages : " + messages);
		client.disconnect();

	}
}

class HelloClientHandler extends SimpleChannelInboundHandler<String> {
	private final ChannelPool channelPool;

	public HelloClientHandler(ChannelPool channelPool) {
		Utils.print("Init of HelloClientHandler : " + channelPool);
		this.channelPool = channelPool;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		//Utils.print("Reading response from server : " + msg);
		Attribute<CompletableFuture<String>> futureAttribute = ctx.channel().attr(HelloClient.FUTURE);
		CompletableFuture<String> future = futureAttribute.getAndRemove();
		Attribute<ChannelPool> poolAttr = ctx.channel().attr(HelloClient.FUTURE_POOL);
		ChannelPool pool = poolAttr.getAndRemove();
		
		//Utils.print("Releasing channel: ctx=" + ctx + ", Chnnel="+ctx.channel()+ ", channelPool:" + pool);
		pool.release(ctx.channel());
		future.complete(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Attribute<CompletableFuture<String>> futureAttribute = ctx.channel().attr(HelloClient.FUTURE);
		CompletableFuture<String> future = futureAttribute.getAndRemove();
		cause.printStackTrace();
		Attribute<ChannelPool> poolAttr = ctx.channel().attr(HelloClient.FUTURE_POOL);
		ChannelPool pool = poolAttr.getAndRemove();
		Utils.print("Exception: Releasing channel: ctx=" + ctx + ", Chnnel="+ctx.channel()+ ", channelPool:" + pool);
		pool.release(ctx.channel());
		ctx.close();
		future.completeExceptionally(cause);
	}
}

class HelloCLientChannelPoolHandler extends AbstractChannelPoolHandler {
	private ChannelPool channelPool;

	public HelloCLientChannelPoolHandler(ChannelPool channelPool) {
		Utils.print("Init of HelloCLientChannelPoolHandler : " + channelPool);
		this.channelPool = channelPool;
	}

	@Override
	public void channelCreated(Channel ch) {
		Utils.print("Creating channel : " + ch);
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
		pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
		pipeline.addLast("clientHandler", new HelloClientHandler(channelPool));
		Utils.print("Channel created : " + ch);
	}
}
