package com.laz.filesync.client.handler;

import com.laz.filesync.client.msg.DiffFilesMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MsgClientHandler extends SimpleChannelInboundHandler<DiffFilesMsg> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DiffFilesMsg msg) throws Exception {
		
		
	}

}
