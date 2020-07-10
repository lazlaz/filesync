package com.laz.filesync.server;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laz.filesync.client.FileSyncClient;
import com.laz.filesync.conf.Configuration;
import com.laz.filesync.server.handler.FileHandler;
import com.laz.filesync.server.handler.MsgServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 文件同步服务端
 * 
 *
 */
public class FileSyncServer {
	private static Logger logger = LoggerFactory.getLogger(FileSyncServer.class);
	private int port;
	private Configuration conf;

	public FileSyncServer(Configuration conf) {
		this.conf = conf;
		init();
	}

	private void init() {
		this.port = conf.getPort() == 0 ? 8989 : conf.getPort();
	}

	public void start() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();// 处理连接线程组
		EventLoopGroup workGroup = new NioEventLoopGroup();// 处理io线程组

		ServerBootstrap server = new ServerBootstrap();
		server.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)// 指定处理客户端的通道
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast("fileHandler",new FileHandler());
						pipeline.addLast("decoder", new ObjectDecoder(1024*1024,
								ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
						pipeline.addLast("encoder", new ObjectEncoder());
						pipeline.addLast("handler", new MsgServerHandler());
						

					}
				});// 通道初始化
		try {
			logger.info("---------------------服务端启动--------------------");
			ChannelFuture future = server.bind(port).sync();
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}

}
