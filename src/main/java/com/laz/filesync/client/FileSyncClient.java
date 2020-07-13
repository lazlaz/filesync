package com.laz.filesync.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laz.filesync.client.handler.MsgClientHandler;
import com.laz.filesync.client.msg.RequestMsg;
import com.laz.filesync.conf.Configuration;
import com.laz.filesync.server.FileSyncServer;
import com.laz.filesync.server.handler.MsgServerHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class FileSyncClient {
	private int port;
	private String ip;
	private Configuration conf;
	private static Logger logger = LoggerFactory.getLogger(FileSyncClient.class);

	public FileSyncClient(Configuration conf) {
		this.conf = conf;
		init();
	}
	private void init() {
		this.port = conf.getPort() == 0 ? 8989 : conf.getPort();
		this.ip = conf.getServerIP();
		if (this.ip == null) {
			throw new RuntimeException("未找到服务端IP");
		}
	}
	public void start() {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>(){
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						//输入Handler
						pipeline.addLast("decoder", new ObjectDecoder(1024*1024,
								ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
						//输出Handler
						pipeline.addLast("encoder", new ObjectEncoder());
						
						
						MsgClientHandler hanler = new MsgClientHandler();
						hanler.setConf(conf);
						pipeline.addLast("handler", hanler);
					}
				});

		try {
			ChannelFuture future = bootstrap.connect(ip,port).sync();
			logger.info("------------客服端启动----------------");
			future.channel().closeFuture().await();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) {
		Configuration conf = new Configuration();
		conf.setClientPath("d:/filesync/client");
		conf.setServerPath("d:/filesync/server");
		conf.setServerIP("127.0.0.1");
		new FileSyncClient(conf).start();
	}
}
