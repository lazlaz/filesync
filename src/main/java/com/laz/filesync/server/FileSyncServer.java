package com.laz.filesync.server;

import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laz.filesync.conf.Configuration;
import com.laz.filesync.server.file.handler.FileReceiveServerHandler;
import com.laz.filesync.server.handler.MsgServerHandler;
import com.laz.filesync.util.Constants;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * 文件同步服务端
 * 
 *
 */
public class FileSyncServer {
	private static Logger logger = LoggerFactory.getLogger(FileSyncServer.class);
	private int port;
	private int filePort;
	private Configuration conf;

	public FileSyncServer(Configuration conf) {
		this.conf = conf;
		init();
	}

	private void init() {
		this.port = conf.getPort() == 0 ? 8989 : conf.getPort();
		this.filePort = conf.getFilePort() == 0 ? 8990 : conf.getFilePort();
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
						pipeline.addLast("decoder", new ObjectDecoder(Constants.OBJECT_SIZE_LIMIT,
								ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
						pipeline.addLast("encoder", new ObjectEncoder());
						MsgServerHandler handler = new MsgServerHandler();
						pipeline.addLast("handler", handler);
					}
				});// 通道初始化
		try {
			logger.info("---------------------服务端启动--------------------");
			ChannelFuture future = server.bind(port).sync();
			startFileServer();
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}

	private void startFileServer() throws InterruptedException {
		new FileReceiveServer(filePort).start();
	}
	
	public static void main(String[] args) {
		Configuration conf = new Configuration();
		conf.setServerIP("127.0.0.1");
		new FileSyncServer(conf).start();
	}

}
