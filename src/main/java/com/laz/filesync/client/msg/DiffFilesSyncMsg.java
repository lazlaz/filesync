package com.laz.filesync.client.msg;

import java.io.Serializable;

import com.laz.filesync.msg.BaseMsg;
import com.laz.filesync.msg.MsgType;

/**
 * 服务端接受有差异的文件信息
 *
 */
public class DiffFilesSyncMsg extends BaseMsg{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public MsgType getType() {
		return MsgType.SYNC;
	}

}
