package com.laz.filesync.rysnc.checksums;

import java.util.Arrays;

public class DiffCheckItem {
	
	/**
	 * 是否匹配
	 */
	private boolean isMatch;
	
	/**
	 * 匹配，加入匹配号,设置为long类型，防止srcraf.seek(i*blockSize)超出整数范围，变为负数报错
	 */
	private long index;
	
	/**
	 * 不匹配，写入数据
	 */
	private byte [] data;

	public boolean isMatch() {
		return isMatch;
	}

	public void setMatch(boolean isMatch) {
		this.isMatch = isMatch;
	}

	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "DiffCheckList [isMatch=" + isMatch + ", index=" + index
				+ ", data=" + data.length + "]";
	}

	
	
	

}
