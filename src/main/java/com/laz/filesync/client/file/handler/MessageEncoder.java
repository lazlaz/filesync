package com.laz.filesync.client.file.handler;

import org.apache.log4j.Logger;

import com.laz.filesync.server.msg.FileInfo;
import com.laz.filesync.util.FileSyncUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;

public class MessageEncoder extends MessageToByteEncoder<Object> {
	public static final Logger logger = Logger.getLogger(MessageEncoder.class);
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		if (msg instanceof DefaultFileRegion) {
			DefaultFileRegion file = (DefaultFileRegion)msg;
			ReferenceCountUtil.retain(file);//这行
			ctx.write(file);
		} else if(msg instanceof FileInfo){
			FileInfo info = (FileInfo)msg;
			byte[] fileNames = info.getFilename().getBytes();
			out.writeInt(FileSyncUtil.STAERT_FLAG);
			out.writeInt(fileNames.length);
			out.writeBytes(fileNames);
			out.writeLong(info.getLength());
		} else {
			logger.error("无法识别的消息类型"+msg);
		}
	}

}
