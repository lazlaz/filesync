package com.laz.filesync.server.msg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private Map<String,FileChecksums> checksumsMap;
	
	public Map<String, FileChecksums> getChecksumsMap() {
		return checksumsMap;
	}
	
	public void setChecksumsMap(Map<String, FileChecksums> checksumsMap) {
		this.checksumsMap = checksumsMap;
	}

	@Override
	public MsgType getType() {
		return MsgType.CHECK_SUM;
	}
	
	
	
}
