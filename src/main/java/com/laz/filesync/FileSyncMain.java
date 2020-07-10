package com.laz.filesync;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.laz.filesync.client.FileSyncClient;
import com.laz.filesync.conf.Configuration;
import com.laz.filesync.server.FileSyncServer;
import com.laz.filesync.util.Constants;

public class FileSyncMain {
	public static void main(String[] args) {
		try {
			Configuration conf = parseArgs(args);
			new FileSyncMain().start(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void start(Configuration conf) {
		if (Constants.CLIENT_MODE.equals(conf.getMode())) {
			new FileSyncServer(conf).start();
		} else if (Constants.CLIENT_MODE.equals(conf.getMode())) {
			new FileSyncClient(conf).start();
		} else {
			System.out.println("模式输入不正确");
		}
	}
	private static Configuration parseArgs(String[] args) throws ParseException {
		Configuration conf = new Configuration();
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("m", "mode", true, "以客户端还是服务端模式启动 server:服务端 client:客服端");
		options.addOption("p", "port", false, "运行端口");
		options.addOption("h", "host", false, "服务端地址");
		options.addOption("clientPath",  false, "客服端同步目录地址");
		options.addOption("serverPath",  false, "服务端同步目录地址");
		CommandLine commandLine = parser.parse(options, args);//解析参数
		if (!commandLine.hasOption("m")) {
			throw new RuntimeException("请输入模式");
		}
		conf.setMode(commandLine.getOptionValue("m"));
		if (!commandLine.hasOption("p")) {
			conf.setPort(Integer.parseInt(commandLine.getOptionValue("p")));
		}
		if (!commandLine.hasOption("h")) {
			conf.setServerIP(commandLine.getOptionValue("h"));
		}
		if (!commandLine.hasOption("clientPath")) {
			conf.setClientPath(commandLine.getOptionValue("clientPath"));
		}
		if (!commandLine.hasOption("serverPath")) {
			conf.setServerPath(commandLine.getOptionValue("serverPath"));
		}
		return conf;
	}
}
