package com.laz.filesync.server.file.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class FileReceiveServerHandler extends ChannelInboundHandlerAdapter {
	private long start;
	private long readLength;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("----------------active-------------");
		start = 0;
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf byteBuf = (ByteBuf) msg;
		readLength += byteBuf.readableBytes();
		File file = new File("d:/word/word2.zip");
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
		randomAccessFile.seek(start);// 移动文件记录指针的位置,
		int length = byteBuf.readableBytes();
		start+=length;
		byte[] bytes = new byte[length];
		byteBuf.readBytes(bytes);
		randomAccessFile.write(bytes);// 调用了seek（start）方法，是指把文件的记录指针定位到start字节的位置。也就是说程序将从start字节开始写数据
		byteBuf.release();
		System.out.println(readLength);
		randomAccessFile.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		System.out.println("----------------channelInactive-------------");
	}
}
