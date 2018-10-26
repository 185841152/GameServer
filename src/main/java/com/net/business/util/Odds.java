package com.net.business.util;

public class Odds {
	private int oddsId; // id
	private int odds; // 概率

	// 其他附带信息
	private byte type;// 类型
	private int num;

	public int getOddsId() {
		return oddsId;
	}

	public void setOddsId(int oddsId) {
		this.oddsId = oddsId;
	}

	public int getOdds() {
		return odds;
	}

	public void setOdds(int odds) {
		this.odds = odds;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

}
