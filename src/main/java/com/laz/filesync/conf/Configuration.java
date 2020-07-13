package com.laz.filesync.conf;

/**
 * @author laz
 *
 */
public class Configuration {
	//启动模式
	private String mode;
	//绑定端口
	private int port;
	//文件传输端口
	private int filePort;
	//服务端地址
	private String serverIP;
	private String clientPath;
	private String serverPath;
	
	public void setClientPath(String clientPath) {
		this.clientPath = clientPath;
	}
	public String getClientPath() {
		return clientPath;
	}
	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}
	public String getServerPath() {
		return serverPath;
	}
	
	public String getServerIP() {
		return serverIP;
	}
	
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getPort() {
		return port;
	}
	public String getMode() {
		return mode;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public int getFilePort() {
		return filePort;
	}
	public void setFilePort(int filePort) {
		this.filePort = filePort;
	}
}
