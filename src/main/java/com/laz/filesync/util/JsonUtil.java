package com.laz.filesync.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.util.CharsetUtil;

public class JsonUtil {
	private JsonUtil() {

	}

	public static String toJson(Object o) {
		return JSON.toJSONString(o);
	}

	public static <T> T fromJson(FullHttpRequest request, Class<T> c) {
		ByteBuf jsonBuf = request.content();
		String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
		return JSON.parseObject(jsonStr, new TypeReference<T>() {});
	}

	public static <T> T fromJson(String json, Class<T> c) {
		return JSON.parseObject(json, new TypeReference<T>() {});
	}

	
}
