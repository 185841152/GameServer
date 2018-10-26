package com.net.server.mmo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.net.server.data.IGameArray;
import com.net.server.data.GameArray;
import com.net.server.entities.User;

public class MMOItem extends BaseMMOItem {
	private static final AtomicInteger AUTO_ID = new AtomicInteger();
	private final Map<String, IMMOItemVariable> variables;
	private P3D lastPos;
	private Vec3D lastLoc;
	private volatile List<User> lastProxyList;
	private final int id;
	private MMORoom room;

	public MMOItem() {
		this.id = AUTO_ID.getAndIncrement();
		this.variables = new HashMap<String, IMMOItemVariable>();
	}

	public MMOItem(List<IMMOItemVariable> variables) {
		this();
		setVariables(variables);
	}

	public int getId() {
		return this.id;
	}

	public IMMOItemVariable getVariable(String name) {
		return (IMMOItemVariable) this.variables.get(name);
	}

	public List<IMMOItemVariable> getVariables() {
		List<IMMOItemVariable> vars = null;

		synchronized (this.variables) {
			vars = new LinkedList<IMMOItemVariable>(this.variables.values());
		}

		return vars;
	}

	public void setVariable(IMMOItemVariable var) {
		synchronized (this.variables) {
			this.variables.put(var.getName(), var);
		}
	}

	public void setVariables(List<IMMOItemVariable> varList) {
		synchronized (this.variables) {
			for (IMMOItemVariable itemVar : varList) {
				this.variables.put(itemVar.getName(), itemVar);
			}
		}
	}

	public void removeVariable(String varName) {
		synchronized (this.variables) {
			this.variables.remove(varName);
		}
	}

	public IGameArray toSFSArray() {
		IGameArray arr = new GameArray();

		arr.addInt(this.id);

		arr.addGameArray(getVariablesData());

		return arr;
	}

	public MMORoom getRoom() {
		return this.room;
	}

	List<User> getLastProxyList() {
		return this.lastProxyList;
	}

	void setLastProxyList(List<User> proxyList) {
		this.lastProxyList = proxyList;
	}

	P3D getLastPos() {
		return this.lastPos;
	}

	void setLastPos(P3D pos) {
		this.lastPos = pos;
	}

	Vec3D getLastLocation() {
		return this.lastLoc;
	}

	void setLastLocation(Vec3D loc) {
		this.lastLoc = loc;
	}

	void setRoom(MMORoom room) {
		this.room = room;
	}

	public String toString() {
		return String.format("[id: %s, %s, %s ]",
				new Object[] { Integer.valueOf(this.id), this.lastPos, this.variables });
	}

	private IGameArray getVariablesData() {
		IGameArray arr = new GameArray();

		List<IMMOItemVariable> vars = getVariables();
		for (IMMOItemVariable var : vars) {
			arr.addGameArray(var.toSFSArray());
		}

		return arr;
	}
}