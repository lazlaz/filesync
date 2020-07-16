package com.laz.filesync.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
	public static boolean createFile(File file) throws IOException {
		if (file.exists()) {
			return true;
		}
		File folder = file.getParentFile();
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return file.createNewFile();
	}
	
	/**
	 * linux系统获取的路径是/ window获取的路径是\,为了统一，统一转换成/
	 * @param path
	 * @return
	 */
	public static String convertPath(String path) {
		if (path !=null) {
			path = path.replace("\\", "/");
		}
		return path;
	}
	
	/**
	 * 获取相对路径
	 * @param f 文件
	 * @param relative 相对路径
	 * @return 文件绝对路径减去relative后的路径
	 */
	public static String getRelativePath(File f, String relative) {
		String fPath = f.getAbsolutePath();
		String rp = new File(relative).getAbsolutePath();
		int index = fPath.indexOf(rp) + rp.length();
		String relativepath = index>fPath.length()-1?"":fPath.substring(index+1);
		return relativepath;
	}
}
