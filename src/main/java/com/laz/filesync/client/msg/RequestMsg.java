package com.laz.filesync.client.msg;

import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.msg.MsgType;

public class RequestMsg extends BaseMsg{
	private static final long serialVersionUID = 1L;
	private String folderPath;
	
	public String getFolderPath() {
		return folderPath;
	}
	
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	@Override
	public MsgType getType() {
		return MsgType.REQUEST;
	}

}
