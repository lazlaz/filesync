package com.laz.filesync.server.file.handler;

import java.io.File;
import java.util.Arrays;

import com.laz.filesync.server.msg.FileInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;

public class FileSendClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		File file = new File("d:/word/word.zip");
		writeAndFlushFileRegion(ctx, file);
	}
	
	private void writeAndFlushFileRegion(ChannelHandlerContext ctx, File file) {
		DefaultFileRegion fileRegion = new DefaultFileRegion(file, 0, file.length());
		FileInfo info = new FileInfo();
		info.setFilename(file.getName());
		info.setLength(file.length());
		ctx.writeAndFlush(info);
		ctx.writeAndFlush(fileRegion).addListener(future -> {
			if (future.isSuccess()) {
				System.out.println("发送完成...");
			}
		});
	}

}
