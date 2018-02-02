package one.yate.matedata;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yangting
 * @date 2018/1/24
 * TODO
 */
public class TestMain {

	public static class A extends ChannelInboundHandlerAdapter {
		public int x = 0;

		public A(int x) {
			this.x = x;
		}

		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println("A channelRead" + x + ":" + ((ByteBuf)msg).toString());
			if(x == 1)
				ctx.fireChannelRead(msg);
			else
				ctx.writeAndFlush(msg);
		}
	}

	public static class B extends ChannelOutboundHandlerAdapter {
		public int x = 0;

		public B(int x) {
			this.x = x;
		}

		public void read(ChannelHandlerContext ctx) throws Exception {
			System.out.println("B read" + x);
			ctx.read();
		}

		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			System.out.println("B write" + x);
			ctx.writeAndFlush(msg, promise);
		}
	}

	public static void main(String... s) {
		ServerBootstrap serverBootstrap = new ServerBootstrap();

		NioEventLoopGroup eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, String.format("NettyBoss_%d", this.threadIndex.incrementAndGet()));
			}
		});

		NioEventLoopGroup eventLoopGroupSelector = new NioEventLoopGroup(1, new ThreadFactory() {
			private AtomicInteger threadIndex = new AtomicInteger(0);
			private int threadTotal = 1;

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
			}
		});

		ServerBootstrap childHandler =
				serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupSelector)
						.channel(NioServerSocketChannel.class)
						.option(ChannelOption.SO_BACKLOG, 1024)
						.option(ChannelOption.SO_REUSEADDR, true)
						.option(ChannelOption.SO_KEEPALIVE, false)
						.childOption(ChannelOption.TCP_NODELAY, true)
						.childOption(ChannelOption.SO_SNDBUF, 65535)
						.childOption(ChannelOption.SO_RCVBUF, 65535)
						.localAddress(new InetSocketAddress(9999))
						.childHandler(new ChannelInitializer <SocketChannel>() {
							@Override
							public void initChannel(SocketChannel ch) throws Exception {
								ChannelPipeline cp = ch.pipeline();
								for (int i = 0; i < 2; i++) {
									cp.addLast(new A(i));
									cp.addLast(new B(i));
								}
							}
						});

		try {
			serverBootstrap.bind().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
