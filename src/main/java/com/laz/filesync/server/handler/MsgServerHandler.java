package com.laz.filesync.server.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.laz.filesync.client.msg.RequestMsg;
import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.msg.ErrorMsg;
import com.laz.filesync.msg.MsgType;
import com.laz.filesync.rysnc.checksums.FileChecksums;
import com.laz.filesync.server.msg.FileCheckSumsMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MsgServerHandler extends SimpleChannelInboundHandler<BaseMsg> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BaseMsg msg) throws Exception {
		switch (msg.getType()) {
		case REQUEST: {
			RequestMsg requestMsg = (RequestMsg) msg; 
			BaseMsg response = dealRequestMsg(requestMsg);
			ctx.writeAndFlush(response);
		}
			break;
		case CHECK_SUM: {

		}
			break;
		case SYNC: {

		}
			break;
		default:
			break;

		}

	}

	private BaseMsg dealRequestMsg(RequestMsg requestMsg) {
		BaseMsg msg = null;
		//获取客服端相应同步的服务端目录
		String folderPath = requestMsg.getFolderPath();
		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		//必须是目录
		if (folder.isDirectory()) {
			List<FileChecksums> checksums = new ArrayList<FileChecksums>();
			getFileCheckSums(folder,checksums);
			FileCheckSumsMsg checksumsMsg = new FileCheckSumsMsg();
			checksumsMsg.setChecksums(checksums);
			msg = checksumsMsg;
		} else {
			ErrorMsg errorMsg = new ErrorMsg();
			errorMsg.setCode(ErrorMsg.Code.SERVER_PATH_IS_NOT_FOLDER.getCode());
			errorMsg.setMsg(ErrorMsg.Code.SERVER_PATH_IS_NOT_FOLDER.getMsg());
			msg = errorMsg;
		}
		return msg;
	}

	private void getFileCheckSums(File f, List<FileChecksums> list) {
		if (f.isDirectory()) {
			for (File file:f.listFiles())  {
				getFileCheckSums(file,list);
			}
		} else {
			FileChecksums checksums = new FileChecksums(f);
			list.add(checksums);
		}
	}

	/*
	 * 客户端连接到服务器
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}
}
