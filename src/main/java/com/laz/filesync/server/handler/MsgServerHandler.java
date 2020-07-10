package com.laz.filesync.server.handler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.laz.filesync.client.msg.RequestMsg;
import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.msg.ErrorMsg;
import com.laz.filesync.rysnc.checksums.FileChecksums;
import com.laz.filesync.server.msg.FileCheckSumsMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MsgServerHandler extends SimpleChannelInboundHandler<BaseMsg> {
	public static final Logger logger = Logger.getLogger(MsgServerHandler.class);
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
			Map<String,FileChecksums> checksums = new HashMap<String,FileChecksums>();
			getFileCheckSums(folder,folder,checksums);
			FileCheckSumsMsg checksumsMsg = new FileCheckSumsMsg();
			checksumsMsg.setChecksumsMap(checksums);
			msg = checksumsMsg;
		} else {
			ErrorMsg errorMsg = new ErrorMsg();
			errorMsg.setCode(ErrorMsg.Code.SERVER_PATH_IS_NOT_FOLDER.getCode());
			errorMsg.setMsg(ErrorMsg.Code.SERVER_PATH_IS_NOT_FOLDER.getMsg());
			msg = errorMsg;
		}
		return msg;
	}

	private void getFileCheckSums(File root, File f, Map<String, FileChecksums> map) {
		if (f.isDirectory()) {
			for (File file:f.listFiles())  {
				getFileCheckSums(root,file,map);
			}
		} else {
			FileChecksums checksums = new FileChecksums(f);
			String rootPath = root.getAbsolutePath();
			String filePath = f.getAbsolutePath();
			String path = filePath.substring(rootPath.length()+1,filePath.length());
			map.put(path,checksums);
		}
	}

	/*
	 * 客户端连接到服务器
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}
	
	/**
	 * 异常错误处理
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("错误原因：" + cause.getMessage());
		ctx.channel().close();
	}
}
