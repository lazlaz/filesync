package com.laz.filesync.util;

import java.util.HashMap;

/**
 * 路径map,统一路径分隔符识别为/
 * 
 * @author laz
 *
 */
public class PathMap<K,V> extends HashMap<K,V> {
	@Override
	public V get(Object key) {
		if (key instanceof String) {
			String path = (String)key;
			key = (K) FileUtil.convertPath(path);
		}
		return super.get(key);
	}
	
	@Override
	public V put(K key, V value) {
		if (key instanceof String) {
			String path = (String)key;
			key = (K) FileUtil.convertPath(path);
		}
		return super.put(key, value);
	}
}
