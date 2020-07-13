package com.laz.filesync.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;

import com.laz.filesync.rysnc.util.Constants;
import com.laz.filesync.rysnc.util.RsyncException;

public class FileSyncUtil {
	public static int STAERT_FLAG = 0x12345;

	public static synchronized File  getServerTempFile(String initName) {
		String tempPath = System.getProperty("java.io.tmpdir");
		File tempFolder = new File(tempPath);
		if (!tempFolder.exists()) {
			tempFolder.mkdirs();
		}
		// 生成本次需同步的差异文件存放目录
		String serverTempName = initName.replace(".zip", "_server.zip");
		File diffZip = new File(tempFolder + File.separator + serverTempName);
		return diffZip;
	}

	public static byte[] generateFileDigest(File file) {
		FileInputStream fis = null;
		try {
			MessageDigest sha = MessageDigest.getInstance(Constants.MD5);
			fis = new FileInputStream(file);
			byte[] buf = new byte[Constants.BLOCK_SIZE];
			int read = 0;
			while ((read = fis.read(buf)) > 0) {
				sha.update(buf, 0, read);
			}
			return sha.digest();
		} catch (IOException e) {
			throw new RsyncException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RsyncException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new RsyncException(e);
				}
			}
		}
	}

	/**
	 * 获取时间字符串
	 * 
	 * @return
	 */
	public static String getTimeStr() {
		LocalDate d = LocalDate.now();
		int year = d.getYear();
		int month = d.getMonthValue();
		int date = d.getDayOfMonth();

		LocalTime time = LocalTime.now();
		int hour = time.getHour();
		int minute = time.getMinute();
		int secord = time.getSecond();
		return year + "-" + month + "-" + date + "-" + hour + "-" + minute + "-" + secord;
	}

	public static void main(String[] args) {
		System.out.println(getTimeStr());
	}
}
