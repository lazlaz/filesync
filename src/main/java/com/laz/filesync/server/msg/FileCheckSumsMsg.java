package com.laz.filesync.server.msg;

import java.util.ArrayList;
import java.util.List;

import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.msg.MsgType;
import com.laz.filesync.rysnc.checksums.FileChecksums;

/**
 * 
 * 服务器文件检验和结果
 *
 */
public class FileCheckSumsMsg extends BaseMsg{
	private static final long serialVersionUID = 1L;
	//检验和集
	private List<FileChecksums> checksums;
	
	public List<FileChecksums> getChecksums() {
		return checksums;
	}
	
	public void setChecksums(List<FileChecksums> checksums) {
		this.checksums = checksums;
	}

	@Override
	public MsgType getType() {
		return MsgType.CHECK_SUM;
	}
	
	
	
}
