package com.laz.filesync.rysnc.util;

public  class Constants {
	/**
	 * 分块大小
	 */
	public static  int BLOCK_SIZE = 1024*5;
	public static final String MD5 = "MD5";
	
	public static int getBLOCK_SIZE() {
		return BLOCK_SIZE;
	}
	public static void setBLOCK_SIZE(int bLOCK_SIZE) {
		BLOCK_SIZE = bLOCK_SIZE;
	}
	
	

}
