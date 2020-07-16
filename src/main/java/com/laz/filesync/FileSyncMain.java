package com.laz.filesync;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.laz.filesync.client.FileSyncClient;
import com.laz.filesync.conf.Configuration;
import com.laz.filesync.server.FileSyncServer;
import com.laz.filesync.util.Constants;
import com.laz.filesync.util.FileSyncUtil;

public class FileSyncMain {
	private static Logger logger = LoggerFactory.getLogger(FileSyncMain.class);
	public static void main(String[] args) {
		try {
			Configuration conf = parseArgs(args);
			new FileSyncMain().start(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void start(Configuration conf) {
		if (conf.isClean()) {
			logger.info("开始清空缓存文件");
			long start = System.currentTimeMillis();
			List<File> tempFile = FileSyncUtil.getServerTempFolder();
			for (File file : tempFile) {
				if (file.isDirectory()) {
					try {
						FileUtils.deleteDirectory(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					file.delete();
				}
			}
			long end = System.currentTimeMillis();
			logger.info("结束。耗时"+(end-start)+"ms");
			return ;
		}
		if (Constants.SERVER_MODE.equals(conf.getMode())) {
			new FileSyncServer(conf).start();
		} else if (Constants.CLIENT_MODE.equals(conf.getMode())) {
			new FileSyncClient(conf).start();
		} else {
			logger.info("模式输入不正确");
		}
	}
	private static Configuration parseArgs(String[] args) throws ParseException {
		Configuration conf = new Configuration();
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("m", "mode", true, "以客户端还是服务端模式启动 server:服务端 client:客服端");
		options.addOption("p", "port", true, "运行端口");
		options.addOption("filePort", true, "文件传输监听端口");
		options.addOption("h", "host", true, "客服端需要连接的服务端地址");
		options.addOption("clientPath",  true, "客服端同步目录地址");
		options.addOption("serverPath",  true, "服务端同步目录地址");
		options.addOption("clean",  false, "清空生成缓存文件");
		CommandLine commandLine = parser.parse(options, args);//解析参数
		
		if (commandLine.hasOption("clean")) {
			conf.setClean(true);
			return conf;
		}
		if (!commandLine.hasOption("m")) {
			throw new RuntimeException("请输入模式");
		}
		conf.setMode(commandLine.getOptionValue("m"));
		if (commandLine.hasOption("p")) {
			conf.setPort(Integer.parseInt(commandLine.getOptionValue("p")));
		}
		if (commandLine.hasOption("h")) {
			conf.setServerIP(commandLine.getOptionValue("h"));
		}
		if (commandLine.hasOption("clientPath")) {
			conf.setClientPath(commandLine.getOptionValue("clientPath"));
		}
		if (commandLine.hasOption("serverPath")) {
			conf.setServerPath(commandLine.getOptionValue("serverPath"));
		}
		if (commandLine.hasOption("filePort")) {
			conf.setFilePort(Integer.parseInt(commandLine.getOptionValue("filePort")));
		}
		return conf;
	}
}
