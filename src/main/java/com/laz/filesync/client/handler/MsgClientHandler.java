package com.laz.filesync.client.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.laz.filesync.client.msg.DiffFilesSyncMsg;
import com.laz.filesync.client.msg.RequestMsg;
import com.laz.filesync.conf.Configuration;
import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.rysnc.checksums.DiffCheckItem;
import com.laz.filesync.rysnc.checksums.FileChecksums;
import com.laz.filesync.rysnc.checksums.RollingChecksum;
import com.laz.filesync.rysnc.util.Constants;
import com.laz.filesync.rysnc.util.RsyncFileUtils;
import com.laz.filesync.server.msg.FileCheckSumsMsg;
import com.laz.filesync.util.FileSyncUtil;
import com.laz.filesync.util.ZipUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.channel.SimpleChannelInboundHandler;

public class MsgClientHandler extends SimpleChannelInboundHandler<BaseMsg> {
	public static final Logger logger = Logger.getLogger(MsgClientHandler.class);
	private Configuration conf;
	 //操作系统识别的换行符
    private static final String CR=System.getProperty("line.separator");

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BaseMsg msg) throws Exception {
		switch (msg.getType()) {
		case REQUEST: {

		}
			break;
		case CHECK_SUM: {
			FileCheckSumsMsg checksumsMsg = (FileCheckSumsMsg) msg;
			File tempFolder = getTempFolder();
			//处理消息，获取最终差异文件zip包路径
			String zipPath = dealChecksumsMsg(tempFolder, checksumsMsg);
			RandomAccessFile randomAccessFile = new RandomAccessFile(zipPath, "r");
			FileRegion region = new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
			ctx.write(region);
			// 写入换行符表示文件结束
			ctx.writeAndFlush(CR);
			randomAccessFile.close();
		}
			break;
		case SYNC: {

		}
			break;
		default:
			break;

		}
	}

	private File getTempFolder() {
		String tempPath = System.getProperty("java.io.tmpdir");
		File tempFolder = new File(tempPath);
		if (!tempFolder.exists()) {
			tempFolder.mkdirs();
		}
		// 生成本次需同步的差异文件存放目录
		File diffFolder = new File(tempFolder + File.separator + "diff-" + FileSyncUtil.getTimeStr());
		if (diffFolder.exists()) {
			diffFolder.delete();
		}
		diffFolder.mkdir();
		return diffFolder;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		// 建立连接请求信息
		RequestMsg msg = new RequestMsg();
		msg.setFolderPath(conf.getServerPath());
		ctx.writeAndFlush(msg);
	}

	private String dealChecksumsMsg(File tempFolder, FileCheckSumsMsg checksumsMsg) {
		logger.info("收到服务端的文件检验和集信息" + checksumsMsg.getChecksumsMap().size());
		// 根据检验和生成差异文件信息
		String path = conf.getClientPath();
		File folder = new File(path);
		if (folder.exists() && folder.isDirectory()) {
			try {
				generateDiffFile(folder, folder, checksumsMsg.getChecksumsMap(), tempFolder);
				logger.info("生成同步差异文件到缓存目录" + tempFolder.getAbsolutePath());
				// 形成压缩包
				String zipPath = tempFolder.getAbsolutePath() + ".zip";
				FileOutputStream output = new FileOutputStream(new File(zipPath));
				ZipUtils.toZip(tempFolder.getAbsolutePath(), output, true);
				return zipPath;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			logger.error("客服端文件目录不存在或者文件不是目录");
		}
		return "";
	}

	private void generateDiffFile(File root, File f, Map<String, FileChecksums> checksumsMap, File tempFolder)
			throws Exception {
		if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				generateDiffFile(root, file, checksumsMap, tempFolder);
			}
		} else {
			long start = System.currentTimeMillis();
			// 滚动获取文件之间的差异信息
			List<DiffCheckItem> diffList = rollGetDiff(root, f, checksumsMap);
			long end = System.currentTimeMillis();
			logger.info("滚动计算： spend time :" + (long) (end - start) + "ms");
			generateDiffFileOnTempFolder(root, f, diffList, tempFolder);

		}

	}

	private void generateDiffFileOnTempFolder(File root, File f, List<DiffCheckItem> diffList, File tempFolder)
			throws Exception {
		String rootPath = root.getAbsolutePath();
		String filePath = f.getAbsolutePath();
		String path = filePath.substring(rootPath.length(), filePath.length());
		File tempDiffFile = new File(tempFolder + path);
		if (!tempDiffFile.exists()) {
			tempDiffFile.createNewFile();
		}
		// 生成临时变量文件
		RsyncFileUtils.createRsyncFile(diffList, tempDiffFile, Constants.BLOCK_SIZE);

	}

	private List<DiffCheckItem> rollGetDiff(File root, File f, Map<String, FileChecksums> checksumsMap) {
		String rootPath = root.getAbsolutePath();
		String filePath = f.getAbsolutePath();
		String path = filePath.substring(rootPath.length() + 1, filePath.length());
		FileChecksums check = checksumsMap.get(path);
		List<DiffCheckItem> diffList = new ArrayList<DiffCheckItem>();
		if (check != null) {
			RollingChecksum rck = new RollingChecksum(check, f, diffList);
			rck.rolling();
		}
		return diffList;
	}

	/**
	 * 异常错误处理
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("错误原因：" + cause.getMessage());
		ctx.channel().close();
	}

}
