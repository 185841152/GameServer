package com.net.server.entities.variables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Variables {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, ? extends Variable> toVariablesMap(List<? extends Variable> varList) {
		Map varMap = new HashMap();

		for (Variable vv : varList) {
			varMap.put(vv.getName(), vv);
		}

		return varMap;
	}
}