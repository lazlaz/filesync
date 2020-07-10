package com.laz.filesync.msg;

import java.io.Serializable;

/**
 * 消息父类
 *
 */
public abstract class BaseMsg implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public abstract MsgType getType();
	
}
