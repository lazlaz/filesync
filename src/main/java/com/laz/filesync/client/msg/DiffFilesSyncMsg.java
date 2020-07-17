package com.laz.filesync.client.msg;

import java.util.Set;

import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.msg.MsgType;

/**
 * 服务端接受有差异的文件信息
 *
 */
public class DiffFilesSyncMsg extends BaseMsg{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fileDigest;
	private long length;
	private String fileName;
	private String serverPath;
	private Set<String> noSynsFileSets;
	
	public void setNoSynsFileSets(Set<String> noSynsFileSets) {
		this.noSynsFileSets = noSynsFileSets;
	}
	
	public Set<String> getNoSynsFileSets() {
		return noSynsFileSets;
	}
	public String getServerPath() {
		return serverPath;
	}
	
	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	@Override
	public MsgType getType() {
		return MsgType.SYNC;
	}
	
	public void setFileDigest(String fileDigest) {
		this.fileDigest = fileDigest;
	}
	
	public String getFileDigest() {
		return fileDigest;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
}
