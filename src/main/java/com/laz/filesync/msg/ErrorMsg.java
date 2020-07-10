package com.laz.filesync.msg;

public class ErrorMsg extends BaseMsg {
	private static final long serialVersionUID = 1L;

	public enum Code {
		SERVER_PATH_IS_NOT_FOLDER(1, "服务端目录路径不是目录");

		private int code;
		private String msg;

		private Code(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public int getCode() {
			return this.code;
		}

		public String getMsg() {
			return this.msg;
		}
	}
	private int code;
	private String msg;

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	@Override
	public MsgType getType() {
		return MsgType.ERROR;
	}

}
