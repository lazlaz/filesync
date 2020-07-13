package com.laz.filesync.server.msg;

public class FileInfo {
	private String filename;
	private long length;
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getFilename() {
		return filename;
	}
	public void setLength(long length) {
		this.length = length;
	}
	
	public long getLength() {
		return length;
	}
}
