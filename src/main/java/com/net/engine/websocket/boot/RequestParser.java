package com.net.engine.websocket.boot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

public class RequestParser {

	/**
	 * 解析请求参数
	 * 
	 * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
	 *
	 */
	public static Map<String, String> parse(FullHttpRequest fullReq) throws Exception {
		HttpMethod method = fullReq.method();

		Map<String, String> parmMap = new HashMap<>();

		if (HttpMethod.GET == method) {
			// 是GET请求
			QueryStringDecoder decoder = new QueryStringDecoder(fullReq.uri());
			decoder.parameters().entrySet().forEach(entry -> {
				// entry.getValue()是一个List, 只取第一个元素
				parmMap.put(entry.getKey(), entry.getValue().get(0));
			});
		} else if (HttpMethod.POST == method) {
			// 是POST请求
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullReq);
			decoder.offer(fullReq);

			List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

			for (InterfaceHttpData parm : parmList) {

				Attribute data = (Attribute) parm;
				parmMap.put(data.getName(), data.getValue());
			}

		} else {
			// 不支持其它方法
			throw new Exception("不支持的请求类型"); // 这是个自定义的异常, 可删掉这一行
		}

		return parmMap;
	}
}