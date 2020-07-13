package com.laz.filesync.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laz.filesync.server.file.handler.FileReceiveServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class FileReceiveServer {
	private int port;
	private static Logger logger = LoggerFactory.getLogger(FileReceiveServer.class);
	public FileReceiveServer(int port) {
		this.port = port;
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
						pipeline.addLast(new FileReceiveServerHandler());
						
					}
				});// 通道初始化
		try {
			logger.info("---------------------文件传输端口启动--------------------");
			ChannelFuture future = server.bind(port).sync();
			future.sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new FileReceiveServer(8990).start();
	}
}
