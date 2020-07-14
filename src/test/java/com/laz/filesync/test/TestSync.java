package com.laz.filesync.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.laz.filesync.rysnc.checksums.DiffCheckItem;
import com.laz.filesync.rysnc.checksums.FileChecksums;
import com.laz.filesync.rysnc.checksums.RollingChecksum;
import com.laz.filesync.rysnc.util.Constants;
import com.laz.filesync.rysnc.util.QuickMD5;
import com.laz.filesync.rysnc.util.RsyncFileUtils;

public class TestSync {
	@Test
	public void testCheckSum() throws Exception {
		File srcFile = new File("D:\\filesync\\server\\1.txt");
		File updateFile = new File("D:\\filesync\\client\\1.txt");
		System.out.println(QuickMD5.getFileMD5Buffer(srcFile));
		System.out.println(RsyncFileUtils.checkFileSame(updateFile, srcFile));
	}
	@Test
	public void testRolling() throws Exception {

		File srcFile = new File("D:\\filesync\\server\\1.txt");
		File updateFile = new File("D:\\filesync\\client\\1.txt");
		File tmp = new File("D:\\filesync\\server\\1.txt_tmp");
		File newFile = new File("D:\\filesync\\server\\1.txt_new");
		long t1 = System.currentTimeMillis();
		if (!tmp.exists()) {
			tmp.createNewFile();
		}
		List<DiffCheckItem> dciList = roll(srcFile, updateFile);

		long t2 = System.currentTimeMillis();

		System.out.println("滚动计算： spend time :" + (long) (t2 - t1) + "ms");
		RsyncFileUtils.createRsyncFile(dciList, tmp, Constants.BLOCK_SIZE);

		System.out.println("实际需要传输的大小  :" + tmp.length() + " byte ");

		long t3 = System.currentTimeMillis();

		System.out.println("生成临时文件，耗时 :" + (long) (t3 - t2) + "ms");

		RsyncFileUtils.combineRsyncFile(srcFile, newFile, tmp);

		long t4 = System.currentTimeMillis();

		System.out.println("合并文件  耗时:" + (long) (t4 - t3) + "ms");

		System.out.println("all spend time :" + (long) (t4 - t1) + "ms");

		System.out.println(RsyncFileUtils.checkFileSame(updateFile, newFile));
	}

	private List<DiffCheckItem> roll(File srcFile, File updateFile) {

		FileChecksums fc = new FileChecksums(srcFile);

		List<DiffCheckItem> diffList = new ArrayList<DiffCheckItem>();

		RollingChecksum rck = new RollingChecksum(fc, updateFile, diffList);

		rck.rolling();

		return diffList;
	}

}
