package com.laz.filesync.server.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.laz.filesync.client.msg.DiffFilesSyncMsg;
import com.laz.filesync.client.msg.RequestMsg;
import com.laz.filesync.conf.Configuration;
import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.msg.ErrorMsg;
import com.laz.filesync.rysnc.checksums.FileChecksums;
import com.laz.filesync.rysnc.util.RsyncFileUtils;
import com.laz.filesync.server.msg.FileCheckSumsMsg;
import com.laz.filesync.util.Coder;
import com.laz.filesync.util.FileSyncUtil;
import com.laz.filesync.util.ZipUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MsgServerHandler extends SimpleChannelInboundHandler<BaseMsg> {
	public static final Logger logger = Logger.getLogger(MsgServerHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BaseMsg msg) throws Exception {
		switch (msg.getType()) {
		case REQUEST: {
			RequestMsg requestMsg = (RequestMsg) msg;
			BaseMsg response = dealRequestMsg(requestMsg);
			ctx.writeAndFlush(response);
		}
			break;
		case CHECK_SUM: {

		}
			break;
		case SYNC: {
			DiffFilesSyncMsg diffMsg = (DiffFilesSyncMsg) msg;
			try {
				combineRsyncFile(diffMsg);
				logger.info("文件同步完成，发送服务端进行同步结果验证");
				BaseMsg m = getCheckSumsMsg(new File(diffMsg.getServerPath()));
				ctx.writeAndFlush(m);
			}catch(Exception e) {
				logger.error(ctx.channel().remoteAddress()+"文件同步失败，请客服端重新尝试"+e.getMessage());
				e.printStackTrace();
			}
		}
			break;
		default:
			break;

		}

	}

	private void combineRsyncFile(DiffFilesSyncMsg diffMsg) throws Exception {
		// 进行文件包完整性验证
		File serverFile = FileSyncUtil.getServerTempFile(diffMsg.getFileName());
		boolean v = verify(serverFile, diffMsg.getFileDigest());
		if (v) {
			logger.info("diff包文件完整性校验一致");
			logger.info("文件解压开始--------------");
			long start = System.currentTimeMillis();
			String fileName = serverFile.getName().replace(".zip", "");
			String unzipPath = System.getProperty("java.io.tmpdir") + File.separator + fileName;
			ZipUtils.unzipFile(serverFile, unzipPath);
			String filepath = unzipPath+File.separator+fileName.substring(0, fileName.indexOf("_server"));
			long end = System.currentTimeMillis();
			logger.info("文件解压结束--------------" + (end - start) + "ms");
			// 遍历文件夹，获取同步文件元数据集合信息(key 文件相对路径，文件实际路径)
			Map<String, String> pathMap = new HashMap<String, String>();
			Map<String, Boolean> typeMap = new HashMap<String, Boolean>();
			getFileMap(filepath, filepath, pathMap, typeMap);
			// 进行存在的文件合并
			List<String> exists = new ArrayList<String>();
			combineExistsFile(new File(diffMsg.getServerPath()),diffMsg.getServerPath(), pathMap, exists);
			end = System.currentTimeMillis();
			logger.info("完成存在的文件合并" + (end - start) + "ms");
			// 没有找打的文件或文件夹进行新建
			newCreateFile(typeMap, filepath, diffMsg.getServerPath(), exists);
			end = System.currentTimeMillis();
			logger.info("完成不存在的文件新建" + (end - start) + "ms");
			logger.info("----------------完成文件同步------------------" + (end - start) + "ms");
		} else {
			logger.error("文件完整性校验不一致");
		}
	}

	private void newCreateFile(Map<String, Boolean> typeMap, String srcPath, String desPath, List<String> exists)
			throws IOException {
		for (String k : typeMap.keySet()) {
			if (!exists.contains(k)) {
				File f = new File(desPath + File.separator + k);
				// 判断文件类型
				if (typeMap.get(k)) {
					f.mkdirs();
				} else {
					f.createNewFile();
					FileInputStream in = new FileInputStream(new File(srcPath + File.separator + k));
					FileOutputStream out = new FileOutputStream(f);
					IOUtils.copy(in,out);
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
				}
			}
		}
	}

	private void combineExistsFile(File f, String serverPath, Map<String, String> map, List<String> exists) throws IOException {
		if (serverPath == null) {
			return;
		}
		if (f.isDirectory()) {
			if (f.listFiles().length == 0) {
				// 空目录删除
				String path = map.get(getRelativePath(f, serverPath));
				if (path == null) {
					f.delete();
				}
			} else {
				for (File file : f.listFiles()) {
					combineExistsFile(file,serverPath, map, exists);
				}
			}
		} else {
			String rynscFilePath = map.get(getRelativePath(f, serverPath));
			boolean exist = false;
			if (rynscFilePath != null) {
				File rsyncFile = new File(rynscFilePath);
				if (rsyncFile.isFile()) {
					String newFilePath = f.getAbsolutePath() + FileSyncUtil.NEW_FILE_FLAG;
					File newFile = new File(newFilePath);
					RsyncFileUtils.combineRsyncFile(f, newFile, rsyncFile);
					boolean flag = f.delete();
					if (flag) {
						newFile.renameTo(f);
					} else {
						logger.error(f.getAbsoluteFile()+"文件不能被删除,检查是否文件被占用或者流未关闭");
						throw new RuntimeException(f.getAbsoluteFile()+"文件不能被删除,检查是否文件被占用或者流未关闭");
					}
					exists.add(getRelativePath(f, serverPath));
					exist = true;
				}
			}
			// 不存在进行删除
			if (!exist) {
				if (!f.delete()) {
					logger.error(f.getAbsoluteFile()+"文件不能被删除,检查是否文件被占用或者流未关闭");
					throw new RuntimeException(f.getAbsoluteFile()+"文件不能被删除,检查是否文件被占用或者流未关闭");
				}
			}
		}

	}
	public static void main(String[] args) {
		File f = new File("D:\\filesync\\server\\1.txt_new_rsync_file74565");
		f.renameTo(new File("D:\\\\filesync\\\\server\\\\1.txt"));
	}
	private String getRelativePath(File f, String relative) {
		String fPath = f.getAbsolutePath();
		String rp = new File(relative).getAbsolutePath();
		int index = fPath.indexOf(rp) + rp.length();
		String relativepath = index>fPath.length()-1?"":fPath.substring(index+1);
		return relativepath;
	}

	private void getFileMap(String filepath, String relative, Map<String, String> pathMap,
			Map<String, Boolean> typeMap) {
		File f = new File(filepath);
		String relativepath = getRelativePath(f, relative);
		pathMap.put(relativepath, f.getAbsolutePath());
		if (f.isDirectory()) {
			typeMap.put(relativepath, true);
			for (File file : f.listFiles()) {
				getFileMap(file.getAbsolutePath(), relative, pathMap, typeMap);
			}
		} else {
			typeMap.put(relativepath, false);
		}
	}

	private boolean verify(File serverFile, String fileDigest) throws Exception {
		String digest1 = Coder.encryptBASE64(FileSyncUtil.generateFileDigest(serverFile));
		if (digest1.equals(fileDigest)) {
			return true;
		}
		return false;
	}

	private BaseMsg dealRequestMsg(RequestMsg requestMsg) {
		BaseMsg msg = null;
		// 获取客服端相应同步的服务端目录
		String folderPath = requestMsg.getFolderPath();
		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		// 必须是目录
		if (folder.isDirectory()) {
			msg = getCheckSumsMsg(folder);
		} else {
			ErrorMsg errorMsg = new ErrorMsg();
			errorMsg.setCode(ErrorMsg.Code.SERVER_PATH_IS_NOT_FOLDER.getCode());
			errorMsg.setMsg(ErrorMsg.Code.SERVER_PATH_IS_NOT_FOLDER.getMsg());
			msg = errorMsg;
		}
		return msg;
	}

	private BaseMsg getCheckSumsMsg(File folder) {
		Map<String, FileChecksums> checksums = new HashMap<String, FileChecksums>();
		FileSyncUtil.getFileCheckSums(folder, folder, checksums);
		FileCheckSumsMsg checksumsMsg = new FileCheckSumsMsg();
		checksumsMsg.setChecksumsMap(checksums);
		return checksumsMsg;
		
	}



	/*
	 * 客户端连接到服务器
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
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
