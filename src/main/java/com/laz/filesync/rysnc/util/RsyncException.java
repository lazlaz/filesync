package com.laz.filesync.rysnc.util;

public class RsyncException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2331722209210513479L;

	public RsyncException() {

	}

	public RsyncException(Throwable throwable) {
		initCause(throwable);
	}

}
