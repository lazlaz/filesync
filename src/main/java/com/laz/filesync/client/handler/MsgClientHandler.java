package com.laz.filesync.client.handler;

import com.laz.filesync.client.msg.DiffFilesSyncMsg;
import com.laz.filesync.client.msg.RequestMsg;
import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.server.msg.FileCheckSumsMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MsgClientHandler extends SimpleChannelInboundHandler<BaseMsg> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BaseMsg msg) throws Exception {
		switch (msg.getType()) {
		case REQUEST: {
			
		}
			break;
		case CHECK_SUM: {
			FileCheckSumsMsg checksumsMsg = (FileCheckSumsMsg) msg; 
			BaseMsg response = dealChecksumsMsg(checksumsMsg);
		}
			break;
		case SYNC: {

		}
			break;
		default:
			break;

		}
	}

	private BaseMsg dealChecksumsMsg(FileCheckSumsMsg checksumsMsg) {
		System.out.println("收到服务端的文件检验和集信息");
		System.out.println(checksumsMsg.getChecksums().size());
		return null;
	}

}
