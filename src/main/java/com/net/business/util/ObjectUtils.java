package com.net.business.util;

import org.nustaq.serialization.FSTConfiguration;

public class ObjectUtils {

	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

	public static byte[] serialize(Object obj) {
		return conf.asByteArray(obj);
	}

	public static Object unserialize(byte[] bytes){
		return conf.asObject(bytes);
	}

}
