package com.laz.filesync.server.handler;

import com.laz.filesync.msg.BaseMsg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class FileHandler extends ChannelInboundHandlerAdapter {   
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof BaseMsg) {
			//交于下一个handler处理
			 ctx.fireChannelRead(msg);
		} else if(msg instanceof ByteBuf) {
			System.out.println(msg);
			ByteBuf buf = (ByteBuf)msg;
			
				
		}
	}
	
}	
