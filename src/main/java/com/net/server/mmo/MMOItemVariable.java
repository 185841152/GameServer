package com.net.server.mmo;

import com.net.server.data.IGameArray;
import com.net.server.entities.variables.GameUserVariable;
import com.net.server.entities.variables.VariableType;

public class MMOItemVariable extends GameUserVariable implements IMMOItemVariable {
	public MMOItemVariable(String name, Object value) {
		super(name, value, false);
	}

	public MMOItemVariable(String name, Object value, boolean isHidden) {
		super(name, value, isHidden);
	}

	public static MMOItemVariable newInstance(String name, Object value) {
		return new MMOItemVariable(name, value);
	}

	public static MMOItemVariable newFromStringLiteral(String name, String type, String literal) {
		return new MMOItemVariable(name, type, literal);
	}

	public static MMOItemVariable newFromSFSArray(IGameArray array) {
		return new MMOItemVariable(array.getUtfString(0), array.getElementAt(2));
	}

	protected MMOItemVariable(String name) {
		super(name);
	}

	protected MMOItemVariable(String name, VariableType type, String literal) {
		super(name, type, literal);
	}

	protected MMOItemVariable(String name, String type, String literal) {
		this(name, VariableType.fromString(type), literal);
	}
}