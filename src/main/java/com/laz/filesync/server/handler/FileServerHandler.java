package com.laz.filesync.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laz.filesync.msg.BaseMsg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class FileServerHandler extends ChannelInboundHandlerAdapter {
	public static final Logger logger = LoggerFactory.getLogger(FileServerHandler.class);
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof BaseMsg) {
			// 交于下一个handler处理
			ctx.fireChannelRead(msg);
		} else if (msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf)msg;
		}
	}
	
	/**
	 * 异常错误处理
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		logger.error("错误原因：" + cause.getMessage());
		ctx.channel().close();
	}

}
