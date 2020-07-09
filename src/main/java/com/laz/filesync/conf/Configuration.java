package com.laz.filesync.conf;

public class Configuration {
	//启动模式
	private String mode;
	//绑定端口
	private int port;
	//服务端地址
	private String serverIP;
	
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
}
