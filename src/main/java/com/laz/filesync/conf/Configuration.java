package com.laz.filesync.conf;

public class Configuration {
	private String mode;
	private int port;
	
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
