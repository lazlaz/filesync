package com.laz.filesync.server.file.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laz.filesync.server.FileSyncServer;
import com.laz.filesync.util.FileSyncUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class FileReceiveServerHandler extends ChannelInboundHandlerAdapter {
	private static Logger logger = LoggerFactory.getLogger(FileReceiveServerHandler.class);
	private long start;
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("----------------active-------------");
		start = 0;
	}
	private long fileLen;
	private String fileName; 
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf byteBuf = (ByteBuf) msg;
		if (FileSyncUtil.STAERT_FLAG == byteBuf.getInt(0)) {
			byteBuf.readInt();
			int nameLen = byteBuf.readInt();
			byte[] b = new byte[nameLen];
			byteBuf.readBytes(b);
			fileName = new String(b);
			fileLen = byteBuf.readLong();
			logger.info(fileLen+" "+new String(b));
			logger.info("fileName"+fileName);
		}
		if (fileName==null) {
			logger.error("无法获取同步包文件名"+fileName);
			return ;
		}
		File file = FileSyncUtil.getServerTempFile(fileName);
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
		randomAccessFile.seek(start);// 移动文件记录指针的位置,
		int length = byteBuf.readableBytes();
		start += length;
		byte[] bytes = new byte[length];
		byteBuf.readBytes(bytes);
		randomAccessFile.write(bytes);// 调用了seek（start）方法，是指把文件的记录指针定位到start字节的位置。也就是说程序将从start字节开始写数据
		byteBuf.release();
		randomAccessFile.close();
		if (start>=fileLen) {
			logger.info(file.getAbsolutePath()+"文件接收完成");
			ctx.close();
		}
	}



	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		System.out.println("----------------channelInactive-------------");
	}
}
