package com.laz.filesync.client.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.laz.filesync.client.FileSendClient;
import com.laz.filesync.client.msg.DiffFilesSyncMsg;
import com.laz.filesync.client.msg.RequestMsg;
import com.laz.filesync.conf.Configuration;
import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.msg.ErrorMsg;
import com.laz.filesync.rysnc.checksums.DiffCheckItem;
import com.laz.filesync.rysnc.checksums.FileChecksums;
import com.laz.filesync.rysnc.checksums.RollingChecksum;
import com.laz.filesync.rysnc.util.Constants;
import com.laz.filesync.rysnc.util.RsyncFileUtils;
import com.laz.filesync.server.msg.FileCheckSumsMsg;
import com.laz.filesync.server.msg.FileInfo;
import com.laz.filesync.util.Coder;
import com.laz.filesync.util.FileSyncUtil;
import com.laz.filesync.util.FileUtil;
import com.laz.filesync.util.PathMap;
import com.laz.filesync.util.ZipUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;

public class MsgClientHandler extends SimpleChannelInboundHandler<BaseMsg> {
	public static final Logger logger = Logger.getLogger(MsgClientHandler.class);
	private Configuration conf;
	// 操作系统识别的换行符
	private static final String CR = System.getProperty("line.separator");

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
			if (!checkExitDiff(checksumsMsg)) {
				logger.info("与服务端目录存在差异，开始进行文件同步");
				File tempFolder = getTempFolder();
				// 处理消息，获取最终差异文件zip包路径
				String zipPath = dealChecksumsMsg(tempFolder, checksumsMsg);
				sendFile(ctx, zipPath);
			} else {
				logger.info("服务端与客服端无差异，同步成功");
				ctx.close();
			}
		}
			break;
		case SYNC: {

		}
			break;
		case ERROR: {
			ErrorMsg error = (ErrorMsg) msg;
			logger.error("code" + error.getCode() + " msg:" + error.getMsg());
			ctx.channel().close();
		}
			break;
		default:
			break;

		}
	}

	private boolean checkExitDiff(FileCheckSumsMsg checksumsMsg) throws Exception {
		Map<String, FileChecksums> clientChecksums = new PathMap<String, FileChecksums>();
		File clientFolder = new File(conf.getClientPath());
		logger.info("生成客服端文件检验和信息");
		long start = System.currentTimeMillis();
		FileSyncUtil.getFileCheckSums(clientFolder, clientFolder, clientChecksums);
		long end = System.currentTimeMillis();
		logger.info("客服端文件检验和信息生成完成"+(end-start)+"ms");
		return checkChecksums(checksumsMsg.getChecksumsMap(), clientChecksums);
	}

	private boolean checkChecksums(Map<String, FileChecksums> serverSums, Map<String, FileChecksums> clientSums)
			throws Exception {
		if (serverSums.size() != clientSums.size()) {
			return false;
		}
		for (String k : serverSums.keySet()) {
			FileChecksums serverCheck = serverSums.get(k);
			FileChecksums clientCheck = clientSums.get(k);
			if (clientCheck == null || !Coder.encryptBASE64(clientCheck.getChecksum())
					.equals(Coder.encryptBASE64(serverCheck.getChecksum()))) {
				return false;
			}
		}
		return true;
	}

	private void sendFile(ChannelHandlerContext ctx, String zipPath) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FileSendClient fileClient = connectFileSever();
				try {
					Channel channel = fileClient.getChannel();
					if (channel != null && channel.isActive()) {
						File file = new File(zipPath);
						logger.info(
								"总共传输差异文件容量= " + FileSyncUtil.getDoubleValue((double) file.length() / 1024 / 1024) + "m");
						DefaultFileRegion fileRegion = new DefaultFileRegion(file, 0, file.length());
						FileInfo info = new FileInfo();
						info.setFilename(file.getName());
						info.setLength(file.length());
						channel.writeAndFlush(info);
						System.out.println("线程名：" + Thread.currentThread().getName());
						channel.writeAndFlush(fileRegion).addListener(future -> {
							if (future.isSuccess()) {
								logger.info(file.getAbsolutePath() + "文件传输完成");
								// 通知服务端进行md5验证传输完整性，并进行文件合并
								DiffFilesSyncMsg msg = new DiffFilesSyncMsg();
								msg.setFileDigest(Coder.encryptBASE64(FileSyncUtil.generateFileDigest(file)));
								msg.setLength(file.length());
								msg.setFileName(file.getName());
								msg.setServerPath(conf.getServerPath());
								ctx.writeAndFlush(msg);
							}
						});
					} else {
						logger.error("连接文件服务器失败");
					}
					channel.closeFuture().sync();	
				}catch(Exception e) {
					e.printStackTrace();
				}finally {
					logger.info("关闭与文件传输服务端连接");
					fileClient.getGroup().shutdownGracefully();
				}
			
			}
		}).start();

	}

	private FileSendClient connectFileSever() {
		FileSendClient fileClient = new FileSendClient(conf.getServerIP(), conf.getFilePort());
		fileClient.start();
		return fileClient;
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
				logger.info("打印rsync算法超过5s的文件");
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
			String rootPath = root.getAbsolutePath();
			String path = FileUtil.getRelativePath(f, rootPath);
			FileChecksums check = checksumsMap.get(FileUtil.convertPath(path));
			FileChecksums sourceCheckSum = new FileChecksums(f,false);
			//先判断文件md5是否一致
			if (check!=null && Coder.encryptBASE64(check.getChecksum()).equals(Coder.encryptBASE64(sourceCheckSum.getChecksum()))) {
				logger.info(f.getAbsolutePath()+"文件检验和一致，不需要同步");
			} else {
				// 滚动获取文件之间的差异信息
				List<DiffCheckItem> diffList = rollGetDiff(root, f, checksumsMap);
				long end = System.currentTimeMillis();
				if ((end-start)>5000) {
					logger.info("滚动计算"+f.getAbsoluteFile()+"： spend time :" + (long) (end - start) + "ms");
				}
				generateDiffFileOnTempFolder(root, f, diffList, tempFolder);
			}

		}

	}

	private void generateDiffFileOnTempFolder(File root, File f, List<DiffCheckItem> diffList, File tempFolder)
			throws Exception {
		String rootPath = root.getAbsolutePath();
		String filePath = f.getAbsolutePath();
		String path = FileUtil.getRelativePath(f, rootPath);
		File tempDiffFile = new File(tempFolder + path);
		FileUtil.createFile(tempDiffFile);
		if (diffList == null) {
			// 不存在diff,说明服务端不存在改文件，直接加入同步目录
			FileInputStream in = new FileInputStream(new File(filePath));
			FileOutputStream out = new FileOutputStream(tempDiffFile);
			IOUtils.copy(in, out);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		} else {
			// 生成临时变量文件
			RsyncFileUtils.createRsyncFile(diffList, tempDiffFile, Constants.BLOCK_SIZE);
		}

	}

	private List<DiffCheckItem> rollGetDiff(File root, File f, Map<String, FileChecksums> checksumsMap) throws Exception {
		String rootPath = root.getAbsolutePath();
		String path = FileUtil.getRelativePath(f, rootPath);
		FileChecksums check = checksumsMap.get(FileUtil.convertPath(path));
		List<DiffCheckItem> diffList = new ArrayList<DiffCheckItem>();
		if (check != null) {
			RollingChecksum rck = new RollingChecksum(check, f, diffList);
			rck.rolling();
		} else {
			return null;
		}
		return diffList;
	}

	/**
	 * 异常错误处理
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		logger.error("错误原因：" + cause.getMessage());
		ctx.channel().close();
	}

}
