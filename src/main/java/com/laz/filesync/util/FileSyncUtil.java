package com.laz.filesync.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.laz.filesync.rysnc.checksums.FileChecksums;
import com.laz.filesync.rysnc.util.RsyncException;

public class FileSyncUtil {
	public static int STAERT_FLAG = 0x12345;
	public static String NEW_FILE_FLAG = "_new_rsync_file" + STAERT_FLAG;

	public static void getFileCheck(File root, File f, Map<String, FileChecksums> map,boolean blockCheck) {
		if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				getFileCheck(root, file, map,blockCheck);
			}
		} else {
			FileChecksums checksums = new FileChecksums(f,blockCheck);
			String rootPath = root.getAbsolutePath();
			String path = FileUtil.getRelativePath(f, rootPath);
			map.put(FileUtil.convertPath(path), checksums);
		}
	}
	public static void getFileCheckSums(File root, File f, Map<String, FileChecksums> map) {
		getFileCheck(root, f, map, false);
	}
	public static void getFileCheckSumsAndBlockSums(File root, File f, Map<String, FileChecksums> map) {
		getFileCheck(root, f, map, true);
	}
	public static synchronized List<File> getServerTempFolder() {
		String tempPath = System.getProperty("java.io.tmpdir");
		File tempFolder = new File(tempPath);
		List<File> list = new ArrayList<File>();
		for (File f:tempFolder.listFiles()) {
			if (f.getName().startsWith(Constants.TEMP_PREFIX)) {
				LocalDate d = LocalDate.now();
				int year = d.getYear();
				if (f.getName().contains(year+"") || f.getName().contains((year+1)+"") || f.getName().contains((year-1)+"")) {
					list.add(f);
				}
			}
		}
		return list;
	}
	public static synchronized File getServerTempFile(String initName) {
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
			MessageDigest sha = MessageDigest.getInstance(com.laz.filesync.rysnc.util.Constants.MD5);
			fis = new FileInputStream(file);
			byte[] buf = new byte[com.laz.filesync.rysnc.util.Constants.BLOCK_SIZE];
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

	private static ThreadLocal<DecimalFormat> threadLocal = new ThreadLocal<DecimalFormat>() {
		protected DecimalFormat initialValue() {
			DecimalFormat df = new DecimalFormat("######0.00");
			return df;
		}
	};

	public static String getDoubleValue(Double d) {
		DecimalFormat df = threadLocal.get();
		return df.format(d);
	}
}
